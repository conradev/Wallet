use crate::freeze::reader::Thawable;
use crate::log::{IndexedLog, Log, TokenTransfer};
use crate::Block;
use ethereum_types::{H256, U256};
use hex::encode;
use log::error;
use rlp::{Decodable, DecoderError, Rlp};

pub struct Receipts {
    pub logs: Vec<IndexedLog>,
}

impl Thawable for Receipts {
    fn thaw_from(block: Block, buf: Vec<u8>) -> Self {
        let logs: Vec<IndexedLog> = Rlp::new(&buf)
            .iter()
            .map(|r| Receipt::decode(&r))
            .inspect(|r| {
                if let Err(e) = r {
                    error!("Failed to read receipt from RLP data: {:?}", e);
                }
            })
            .enumerate()
            .filter_map(|(tx, r)| r.ok().map(|r| (tx as u32, r)))
            .flat_map(|(tx, r)| {
                r.logs
                    .into_iter()
                    .enumerate()
                    .map(move |(idx, l)| l.at_index(block, tx, idx as u32))
            })
            .collect();

        Receipts { logs }
    }
}

struct Receipt {
    logs: Vec<Log>,
}

impl Decodable for Receipt {
    fn decode(rlp: &Rlp) -> Result<Self, DecoderError> {
        let logs = rlp
            .at(2)?
            .iter()
            .map(|r| Log::decode(&r))
            // .inspect(|r| {
            //     if let Err(e) = r {
            //         error!("Failed to read log from RLP data: {:?}", e);
            //     }
            // })
            .filter_map(|r| r.ok())
            .filter(|l| l.is_known())
            .collect();
        Ok(Receipt { logs })
    }
}

impl Decodable for Log {
    fn decode(rlp: &Rlp) -> Result<Self, DecoderError> {
        let topics = rlp.at(1)?;
        let signature = topics.val_at::<H256>(0)?;
        Ok(match signature.as_fixed_bytes() {
            &Log::TRANSFER_SIGNATURE => Log::TokenTransfer(TokenTransfer::decode(rlp)?),
            _ => Log::Unknown,
        })
    }
}

impl Decodable for TokenTransfer {
    fn decode(rlp: &Rlp) -> Result<Self, DecoderError> {
        let contract = rlp.val_at(0)?;
        let topics = rlp.at(1)?;
        let data = rlp.at(2)?.data()?;

        let from = topics.val_at::<H256>(1)?.into();
        let to = topics.val_at::<H256>(2)?.into();
        let value = U256::from_big_endian(data);
        Ok(TokenTransfer {
            contract,
            from,
            to,
            value,
        })
    }
}
