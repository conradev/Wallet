use crate::{Block, H256, U256};
use ethereum_types::Address;
use rlp::{Decodable, DecoderError, Rlp};

#[derive(Debug, Copy, Clone)]
pub struct ERC20Transfer {
    pub contract: Address,
    pub from: Address,
    pub to: Address,
    pub value: U256,
}

impl Decodable for ERC20Transfer {
    fn decode(rlp: &Rlp) -> Result<Self, DecoderError> {
        let contract = rlp.val_at(0)?;
        let topics = rlp.at(1)?;
        let data = rlp.at(2)?.data()?;

        let from = topics.val_at::<H256>(1)?.into();
        let to = topics.val_at::<H256>(2)?.into();
        let value = U256::from_big_endian(data);
        Ok(ERC20Transfer {
            contract,
            from,
            to,
            value,
        })
    }
}

#[derive(Debug, Copy, Clone)]
pub enum Log {
    Transfer(ERC20Transfer),
    Unknown,
}

#[derive(Debug, Copy, Clone, Default)]
pub struct LogLocation {
    pub block: Block,
    pub transaction: u32,
    pub log: u32,
}

#[derive(Debug, Copy, Clone)]
pub struct LogWithLocation {
    pub log: Log,
    pub location: LogLocation,
}

impl Log {
    const TRANSFER_SIGNATURE: [u8; 32] = [
        0xDD, 0xF2, 0x52, 0xAD, 0x1B, 0xE2, 0xC8, 0x9B, 0x69, 0xC2, 0xB0, 0x68, 0xFC, 0x37, 0x8D,
        0xAA, 0x95, 0x2B, 0xA7, 0xF1, 0x63, 0xC4, 0xA1, 0x16, 0x28, 0xF5, 0x5A, 0x4D, 0xF5, 0x23,
        0xB3, 0xEF,
    ];

    pub fn is_known(&self) -> bool {
        !matches!(self, &Log::Unknown)
    }
}

impl Decodable for Log {
    fn decode(rlp: &Rlp) -> Result<Self, DecoderError> {
        let topics = rlp.at(1)?;
        let signature = topics.val_at::<H256>(0)?;
        let log = match signature.as_fixed_bytes() {
            &Log::TRANSFER_SIGNATURE => Log::Transfer(ERC20Transfer::decode(rlp)?),
            _ => Log::Unknown,
        };
        Ok(log)
    }
}
