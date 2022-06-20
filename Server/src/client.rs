use crate::{instrument, oneshot, RpcBody, Sender, SinkExt, SplitSink, StreamExt};
use rand::Rng;
use serde::de::DeserializeOwned;
use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use std::error::Error;
use std::fmt::Debug;
use std::io::Cursor;
use std::sync::Arc;
use tokio::net::UnixStream;
use tokio::sync::Mutex;
use tokio::task::JoinHandle;
use tokio_util::codec::{Decoder, Framed, LinesCodec};

#[derive(Debug)]
pub struct Client {
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
    #[instrument]
    pub async fn new() -> Result<Client, Box<dyn Error>> {
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
                    let _ = sender.send(response);
                }
            }
        });

        return Ok(Client {
            sender,
            listener,
            continuations,
        });
    }

    #[instrument]
    pub async fn request<T: Debug, U: Debug>(&mut self, request: T) -> Result<U, Box<dyn Error>>
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
