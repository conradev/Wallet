use std::error::Error;

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
    for receipts in freezer.read::<Receipts>() {
        database.consume(receipts.into_iter().flat_map(|r| r.logs))
    }
    Ok(())
}

type Block = u32;
