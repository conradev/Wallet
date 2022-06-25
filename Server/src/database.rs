use crate::log::{IndexedLog, Log};
use log::{error, info};
use rayon::iter::plumbing::{Consumer, Folder, Reducer, UnindexedConsumer};
use rayon::prelude::*;
use rusqlite::{CachedStatement, Connection, DropBehavior, Statement, ToSql};
use std::collections::hash_map::Entry;
use std::collections::HashMap;
use std::os::macos::raw::stat;
use std::path::{Path, PathBuf};

#[derive(Debug, Copy, Clone)]
enum Operation {
    Schema,
    InsertERC20Transfer,
}

impl AsRef<str> for Operation {
    fn as_ref(&self) -> &str {
        match self {
            Operation::Schema => include_str!("sql/schema.sql"),
            Operation::InsertERC20Transfer => include_str!("sql/insert_erc20_transfer.sql"),
        }
    }
}

impl Operation {
    fn prepare(self, conn: &Connection) -> Statement {
        conn.prepare(self.as_ref()).unwrap()
    }

    fn prepare_cached(self, conn: &Connection) -> CachedStatement {
        conn.prepare_cached(self.as_ref()).unwrap()
    }
}

pub struct Database {
    conn: Connection,
}

impl Database {
    pub fn new<P: AsRef<Path>>(path: P) -> rusqlite::Result<Database> {
        let mut conn = Connection::open(path)?;
        conn.pragma(None, "journal_mode", "WAL", |row| {
            match &row.get::<_, String>(0)?[..] {
                "wal" => Ok(()),
                other => panic!("this is not ideal {}", other),
            }
        })?;
        conn.execute_batch(Operation::Schema.as_ref())?;
        Ok(Database { conn })
    }

    pub fn consume<I: Iterator<Item = IndexedLog>>(&mut self, iter: I) {
        let mut idx = 0;
        let tx_size = 1_000_000;

        let mut insert_token = Operation::InsertERC20Transfer.prepare(&self.conn);
        let mut transaction = self.conn.unchecked_transaction().unwrap();
        for item in iter {
            if idx < tx_size {
                idx += 1;
            } else {
                idx = 0;
                transaction.commit().unwrap();
                info!("Committed transaction with {} items", tx_size);
                transaction = self.conn.unchecked_transaction().unwrap();
            }

            let result = match item.log {
                Log::TokenTransfer(transfer) => {
                    let mut value = [0u8; 32];
                    transfer.value.to_big_endian(&mut value);

                    insert_token.execute::<&[&dyn ToSql]>(&[
                        &item.block,
                        &item.transaction,
                        &item.index,
                        &transfer.contract.as_bytes(),
                        &transfer.from.as_bytes(),
                        &transfer.to.as_bytes(),
                        &value,
                    ])
                }
                Log::Unknown => Ok(0),
            };
            if let Err(e) = result {
                error!("Error inserting record into database: {:?}", e)
            }
        }

        transaction.commit().unwrap();
    }
}
