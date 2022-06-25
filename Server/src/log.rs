use crate::Block;
use ethereum_types::{Address, H256, U256};

#[derive(Debug, Copy, Clone)]
pub struct TokenTransfer {
    pub contract: Address,
    pub from: Address,
    pub to: Address,
    pub value: U256,
}

#[derive(Debug, Copy, Clone)]
pub enum Log {
    TokenTransfer(TokenTransfer),
    Unknown,
}

impl Log {
    pub fn is_known(&self) -> bool {
        !matches!(self, &Log::Unknown)
    }

    pub fn at_index(&self, block: Block, transaction: u32, index: u32) -> IndexedLog {
        IndexedLog {
            block,
            transaction,
            index,
            log: *self,
        }
    }
}

#[derive(Debug, Copy, Clone)]
pub struct IndexedLog {
    pub block: Block,
    pub transaction: u32,
    pub index: u32,
    pub log: Log,
}

impl Log {
    pub const TRANSFER_SIGNATURE: [u8; 32] = [
        0xDD, 0xF2, 0x52, 0xAD, 0x1B, 0xE2, 0xC8, 0x9B, 0x69, 0xC2, 0xB0, 0x68, 0xFC, 0x37, 0x8D,
        0xAA, 0x95, 0x2B, 0xA7, 0xF1, 0x63, 0xC4, 0xA1, 0x16, 0x28, 0xF5, 0x5A, 0x4D, 0xF5, 0x23,
        0xB3, 0xEF,
    ];
}
