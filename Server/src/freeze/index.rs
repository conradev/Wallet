use crate::Block;
use byteorder::{BigEndian, ByteOrder};
use memmap::Mmap;
use rayon::iter::IntoParallelIterator;
use rayon::prelude::*;
use rayon::slice::ChunksExact;
use std::fs::File;
use std::iter::Peekable;
use std::path::Path;

#[derive(Debug, Copy, Clone)]
pub struct Region {
    pub block: Block,
    pub file: u16,
    pub offset: u32,
    pub len: u32,
}

pub struct Index {
    mapping: Mmap,
    len: usize,
}

impl Index {
    pub fn new<P: AsRef<Path>>(path: P) -> std::io::Result<Index> {
        let mut file = File::open(path)?;
        let mapping = unsafe { Mmap::map(&file)? };
        let len = file.metadata()?.len() as usize;
        Ok(Index { mapping, len })
    }

    pub fn load(self, file_lens: Vec<u32>) -> Vec<Region> {
        (&self.mapping[..self.len])
            .par_windows(12)
            .step_by(6)
            .enumerate()
            .map(|(idx, window)| {
                let block = idx as Block;

                let mut buf = [0; 6];
                buf.copy_from_slice(&window[..6]);
                let start = Location::from(buf);
                let file = start.file;
                let offset = start.offset;

                let mut buf = [0; 6];
                buf.copy_from_slice(&window[6..]);
                let end = Location::from(buf);

                let len = if (start.file == end.file) {
                    end.offset - start.offset
                } else {
                    file_lens[start.file as usize] - start.offset
                };

                Region {
                    block,
                    file,
                    offset,
                    len,
                }
            })
            .collect()
    }
}

#[derive(Debug, Copy, Clone)]
struct Location {
    pub file: u16,
    pub offset: u32,
}

impl From<[u8; 6]> for Location {
    fn from(buf: [u8; 6]) -> Self {
        let file = BigEndian::read_u16(&buf);
        let offset = BigEndian::read_u32(&buf[2..]);
        Location { file, offset }
    }
}
