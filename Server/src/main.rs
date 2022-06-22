extern crate core;

use byteorder::{BigEndian, ByteOrder, LittleEndian, NativeEndian};
use ethereum_types::{Address, H160, H256, U256};
use hex::ToHex;
use memmap::Mmap;
use rayon::prelude::*;
use reader::Reader;
use rlp::{Decodable, DecoderError, Rlp, RlpIterator};
use snap::raw::Decoder;
use std::cmp::Ordering;
use std::convert::Infallible;
use std::error::Error;
use std::fs::File;
use std::io::{Cursor, Seek, SeekFrom};
use std::iter::Peekable;
use std::mem;
use std::mem::size_of;
use std::ops::{Add, Range};
use std::path::{Iter, Path};
use std::slice::from_raw_parts;

mod database;
mod index;
mod log;
mod reader;
mod receipt;

fn main() -> Result<(), Box<dyn Error>> {
    env_logger::init();

    let freezer = Reader::lol("/Users/conradev/Library/Ethereum/geth/chaindata/ancient/receipts")?;
    Ok(())
}

type Block = u32;
