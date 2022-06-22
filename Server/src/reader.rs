use crate::database::DatabaseConsumer;
use crate::index::{Index, Location, RegionIterator};
use crate::log::{Log, LogLocation, LogWithLocation};
use crate::receipt::Receipt;
use log::{error, info};
use memmap::Mmap;
use rayon::iter::{
    FromParallelIterator, IndexedParallelIterator, IntoParallelIterator, IntoParallelRefIterator,
    ParallelBridge, ParallelIterator,
};
use rayon::prelude::ParallelSlice;
use rlp::{Decodable, Rlp};
use snap::raw::Decoder;
use std::fs::File;
use std::path::Path;

pub struct Reader {
    index: Index,
}

impl Reader {
    pub fn lol<P: AsRef<Path>>(path: P) -> std::io::Result<()> {
        let mut index_path = path.as_ref().to_path_buf();
        index_path.set_extension("cidx");
        let index = Index::new(index_path)?;

        let mut chunk_path = path.as_ref().to_path_buf();
        let base_name = chunk_path
            .file_name()
            .unwrap()
            .to_str()
            .unwrap()
            .to_string();

        let mut files = vec![];
        let mut mappings: Vec<Mmap> = vec![];
        for index in 0..usize::MAX {
            chunk_path.set_file_name(format!("{}.{:04}.cdat", base_name, index));
            let file = match File::open(chunk_path.clone()) {
                Ok(f) => f,
                Err(ref e) if e.kind() == std::io::ErrorKind::NotFound => break,
                Err(e) => return Err(e),
            };
            mappings.push(unsafe { Mmap::map(&file)? });
            files.push(file);
        }
        let file_lens: Vec<_> = Result::from_par_iter(
            files
                .par_iter()
                .map(|f| f.metadata().map(|m| m.len() as u32)),
        )?;

        info!("Found {} files in freezer", file_lens.len());

        let regions: Vec<_> = RegionIterator::new(file_lens, index.into_iter())
            .par_bridge()
            .collect();

        let logs = regions.into_par_iter().map(|region| {
            if region.chunk_len < 4 {
                return vec![];
            }

            let start = region.chunk_start as usize;
            let end = start + region.chunk_len as usize;
            let compressed = &mappings[region.file_idx as usize][start..end];

            let mut decoder = Decoder::default();
            let buf = match decoder.decompress_vec(compressed) {
                Ok(b) => b,
                Err(e) => {
                    error!("Failed to read decompressed RLP data: {:?}", e);
                    return vec![];
                }
            };

            let logs: Vec<LogWithLocation> = Rlp::new(&buf)
                .iter()
                .enumerate()
                .filter_map(|(tx, rlp)| match Receipt::decode(&rlp) {
                    Ok(receipt) => Some((tx, receipt.logs)),
                    Err(e) => {
                        error!("Failed to read decompressed RLP data: {:?}", e);
                        None
                    }
                })
                .flat_map(|(tx, logs)| {
                    logs.into_iter().enumerate().map(move |(log_index, log)| {
                        let block = region.block;
                        let transaction = tx as u32;
                        let location = LogLocation {
                            block,
                            transaction,
                            log: log_index as u32,
                        };
                        LogWithLocation { log, location }
                    })
                })
                .collect();

            logs
        });

        let database = DatabaseConsumer::new("/Users/conradev/Projects/Wallet/Server/index.db");
        logs.with_min_len(1_000_000).drive(database);
        Ok(())
    }
}
