use crate::log::{ERC20Transfer, Log, LogLocation, LogWithLocation};
use log::info;
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
    fn prepare_cached(self, conn: &Connection) -> CachedStatement {
        conn.prepare_cached(self.as_ref()).unwrap()
    }
}

#[derive(Debug, Clone)]
pub struct DatabaseConsumer {
    path: PathBuf,
}

pub struct DatabaseFolder {
    conn: Connection,
    index: usize,
}

pub struct UnitReducer;

impl DatabaseConsumer {
    pub fn new<P: AsRef<Path>>(path: P) -> DatabaseConsumer {
        DatabaseConsumer {
            path: path.as_ref().to_path_buf(),
        }
    }
}

impl Consumer<Vec<LogWithLocation>> for DatabaseConsumer {
    type Folder = DatabaseFolder;
    type Reducer = UnitReducer;
    type Result = ();

    fn split_at(self, _index: usize) -> (Self, Self, Self::Reducer) {
        (self.split_off_left(), self, UnitReducer)
    }

    fn into_folder(self) -> Self::Folder {
        DatabaseFolder::new(self.path)
    }

    fn full(&self) -> bool {
        false
    }
}

impl UnindexedConsumer<Vec<LogWithLocation>> for DatabaseConsumer {
    fn split_off_left(&self) -> Self {
        self.clone()
    }

    fn to_reducer(&self) -> Self::Reducer {
        UnitReducer
    }
}

impl DatabaseFolder {
    const TX_SIZE: usize = 100_000;

    pub fn new<P: AsRef<Path>>(path: P) -> DatabaseFolder {
        let mut conn = Connection::open(path).unwrap();
        conn.pragma(None, "journal_mode", "WAL", |row| {
            match &row.get::<_, String>(0)?[..] {
                "wal" => Ok(()),
                other => panic!("this is not ideal {}", other),
            }
        })
        .unwrap();
        conn.execute_batch(Operation::Schema.as_ref()).unwrap();
        conn.execute("BEGIN DEFERRED", []).unwrap();
        let index = 0;
        DatabaseFolder { conn, index }
    }
}

impl Folder<Vec<LogWithLocation>> for DatabaseFolder {
    type Result = ();

    fn consume(mut self, item: Vec<LogWithLocation>) -> Self {
        if item.is_empty() {
            return self;
        }

        if self.index >= DatabaseFolder::TX_SIZE {
            info!("Committing transaction with {} logs", self.index);
            self.conn.execute_batch("COMMIT; BEGIN DEFERRED;").unwrap();
            self.index = 0;
        }
        self.index += item.len();

        for lwl in item {
            let op = match lwl.log {
                Log::Transfer(_) => Operation::InsertERC20Transfer,
                Log::Unknown => panic!(),
            };
            let result = {
                let mut statement = op.prepare_cached(&self.conn);

                let loc = lwl.location;
                match lwl.log {
                    Log::Transfer(transfer) => {
                        let mut value = [0u8; 32];
                        transfer.value.to_big_endian(&mut value);
                        statement.execute::<&[&dyn ToSql]>(&[
                            &loc.block,
                            &loc.transaction,
                            &loc.log,
                            &transfer.contract.as_bytes(),
                            &transfer.from.as_bytes(),
                            &transfer.to.as_bytes(),
                            &value,
                        ])
                    }
                    Log::Unknown => panic!(),
                }
            };

            result.expect("sql error");
        }

        self
    }

    fn complete(self) -> Self::Result {
        self.conn.execute_batch("COMMIT").unwrap();
    }

    fn full(&self) -> bool {
        false
    }
}

impl Reducer<()> for UnitReducer {
    fn reduce(self, _left: (), _right: ()) {}
}
