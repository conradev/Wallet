use crate::oneshot::Canceled;
use ethereum_types::{Address, U256};
use futures::channel::oneshot;
use futures::channel::oneshot::Sender;
use futures::future::Ready;
use futures::stream::SplitSink;
use futures::{future, stream, AsyncWriteExt, SinkExt, StreamExt, TryStreamExt};
use rand::Rng;
use serde::de::DeserializeOwned;
use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use std::error::Error;
use std::fmt::Debug;
use std::future::Future;
use std::io::Cursor;
use std::ops::{Add, Range};
use std::sync::Arc;
use tokio::io::split;
use tokio::net::UnixStream;
use tokio::sync::Mutex;
use tokio::task::JoinHandle;
use tokio::time::Instant;
use tokio_util::codec::{Decoder, Encoder, Framed, LinesCodec, LinesCodecError};

#[derive(Debug, Serialize, Deserialize)]
struct Quantity(U256);

trait Request<T> {}

#[derive(Serialize, Deserialize)]
struct RpcRequest {
    id: i64,
    jsonrpc: &'static str,
}

impl RpcRequest {
    fn new(id: i64) -> RpcRequest {
        RpcRequest { id, jsonrpc: "2.0" }
    }
}

#[derive(Serialize)]
struct RpcBody<T> {
    #[serde(flatten)]
    request: RpcRequest,
    #[serde(flatten)]
    payload: T,
}

impl<T> RpcBody<T> {
    fn new(id: i64, payload: T) -> RpcBody<T> {
        let request = RpcRequest::new(id);
        RpcBody { request, payload }
    }
}

#[derive(Serialize, Deserialize)]
struct JsonResult<T> {
    result: T,
}

struct Client {
    sender: SplitSink<Framed<UnixStream, LinesCodec>, String>,
    listener: JoinHandle<()>,
    continuations: Arc<Mutex<HashMap<i64, Sender<String>>>>,
}

impl Drop for Client {
    fn drop(&mut self) {
        self.listener.abort();
    }
}

impl Client {
    async fn new() -> Result<Client, Box<dyn Error>> {
        #[derive(Deserialize)]
        struct Identifier {
            id: i64,
        }

        let continuations = Arc::new(Mutex::new(HashMap::<i64, Sender<String>>::new()));
        let stream = UnixStream::connect("/Users/conradev/Library/Ethereum/geth.ipc").await?;
        let (sender, mut read) = LinesCodec::new().framed(stream).split();

        let clone = continuations.clone();
        let listener = tokio::spawn(async move {
            while let Some(value) = read.next().await {
                let response = match value {
                    Ok(v) => v,
                    Err(e) => return,
                };

                let id = match serde_json::from_slice::<Identifier>(response.as_bytes()) {
                    Ok(v) => v.id,
                    Err(e) => continue,
                };

                let mut continuations = clone.lock().await;
                if let Some(sender) = continuations.remove(&id) {
                    let a = sender.send(response);
                }
            }
        });

        return Ok(Client {
            sender,
            listener,
            continuations,
        });
    }

    async fn request<T, U: Debug>(&mut self, request: T) -> Result<U, Box<dyn Error>>
    where
        T: Serialize,
        U: DeserializeOwned,
    {
        let id = rand::thread_rng().gen();

        let (sender, receiver) = oneshot::channel();
        self.continuations.lock().await.insert(id, sender);

        let body = RpcBody::new(id, request);
        let string = serde_json::to_string(&body)?;
        self.sender.send(string).await?;

        #[derive(Debug, Deserialize)]
        struct Response<T> {
            result: T,
        }
        let response_body = receiver.await?;

        let response: Response<U> = serde_json::from_reader(Cursor::new(response_body))?;
        Ok(response.result)
    }
}

#[derive(Debug, Serialize, Deserialize)]
struct GetBlockByNumber {
    method: &'static str,
    params: (U256, bool),
}

impl GetBlockByNumber {
    pub fn new(number: U256, hydrate: bool) -> GetBlockByNumber {
        GetBlockByNumber {
            method: "eth_getBlockByNumber",
            params: (number, hydrate),
        }
    }
}

#[derive(Debug, Serialize, Deserialize)]
struct BlockNumber {
    method: &'static str,
    params: (),
}

impl Default for BlockNumber {
    fn default() -> Self {
        BlockNumber {
            method: "eth_blockNumber",
            params: (),
        }
    }
}

#[derive(Debug, Serialize, Deserialize)]
struct Transaction {
    value: String,
    from: Address,
    to: Option<Address>,
}

#[derive(Debug, Serialize, Deserialize)]
struct Block {
    number: String,
    transactions: Vec<Transaction>,
}

#[tokio::main]
async fn main() -> Result<(), Box<dyn Error>> {
    let mut client = Arc::new(Mutex::new(Client::new().await?));

    let number: U256 = client.lock().await.request(BlockNumber::default()).await?;

    let mut instant = Instant::now();
    let mut count = 0;
    let mut current = U256::zero();
    while current < number {
        let clone = client.clone();
        tokio::spawn(async move {
            let mut client = clone.lock().await;
            let block: Block = match client.request(GetBlockByNumber::new(current, true)).await {
                Ok(b) => b,
                Err(ee) => return,
            };
            count += 1;
            println!("{:?}", block);
            if count % 100_000 == 0 {
                println!("{:?}: {:?}", count, instant.elapsed());
                instant = Instant::now();
            }
        });
        current += U256::one();
    }
    Ok(())
}
