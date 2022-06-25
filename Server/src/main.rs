use ::log::info;
use std::error::Error;
use std::time::Instant;

mod database;
mod freeze;
mod log;

use database::Database;
use freeze::Receipts;

fn main() -> Result<(), Box<dyn Error>> {
    env_logger::init();

    let freezer = freeze::Reader::new(
        "/Users/conradev/Library/Ethereum/geth/chaindata/ancient",
        "receipts",
    )?;

    let mut database = Database::new("/Users/conradev/Projects/Wallet/Server/index2.db")?;
    for (idx, receipts) in freezer.read::<Receipts>().enumerate() {
        info!("Finished reading logs from chunk {}", idx);
        database.consume(receipts.into_iter().flat_map(|r| r.logs));
        info!("Finished writing logs to database from chunk {}", idx);
    }
    Ok(())
}

type Block = u32;
