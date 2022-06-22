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
pub struct Location {
    pub block: Block,
    pub file_idx: u16,
    pub chunk_start: u32,
}

impl Location {
    fn chunk_len(&self, len: u32) -> Region {
        Region {
            block: self.block,
            file_idx: self.file_idx,
            chunk_start: self.chunk_start,
            chunk_len: len,
        }
    }
}

#[derive(Debug, Copy, Clone)]
pub struct Region {
    pub block: Block,
    pub file_idx: u16,
    pub chunk_start: u32,
    pub chunk_len: u32,
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

    fn location(&self, block: Block) -> Location {
        let offset = 6 * block as usize;
        let file_idx = BigEndian::read_u16(&self.mapping[offset..]);
        let chunk_start = BigEndian::read_u32(&self.mapping[offset + 2..]);
        Location {
            block,
            file_idx,
            chunk_start,
        }
    }
}

impl IntoIterator for Index {
    type Item = Location;
    type IntoIter = LocationIterator;

    fn into_iter(self) -> Self::IntoIter {
        let len = self.len as u32 / 6;
        LocationIterator {
            index: self,
            block: 0,
            len,
        }
    }
}

pub struct RegionIterator<I>
where
    I: Iterator<Item = Location>,
{
    file_lens: Vec<u32>,
    inner: Peekable<I>,
}

impl<I> RegionIterator<I>
where
    I: Iterator<Item = Location>,
{
    pub fn new(file_lens: Vec<u32>, iterator: I) -> RegionIterator<I> {
        RegionIterator {
            file_lens,
            inner: iterator.peekable(),
        }
    }
}

impl<I> Iterator for RegionIterator<I>
where
    I: Iterator<Item = Location>,
{
    type Item = Region;

    fn next(&mut self) -> Option<Self::Item> {
        let start = match self.inner.next() {
            Some(s) => s,
            None => return None,
        };
        let end = match self.inner.peek() {
            Some(next) => {
                if start.file_idx == next.file_idx {
                    next.chunk_start
                } else {
                    self.file_lens[start.file_idx as usize]
                }
            }
            None => self.file_lens[start.file_idx as usize],
        };

        Some(start.chunk_len(end - start.chunk_start))
    }
}

pub struct LocationIterator {
    index: Index,
    block: u32,
    len: u32,
}

impl Iterator for LocationIterator {
    type Item = Location;

    fn next(&mut self) -> Option<Self::Item> {
        if self.block < self.len {
            let location = self.index.location(self.block);
            self.block += 1;
            Some(location)
        } else {
            None
        }
    }
}

impl ExactSizeIterator for LocationIterator {
    fn len(&self) -> usize {
        self.len as usize
    }
}

struct Chunk {
    mapping: Mmap,
}

impl Chunk {
    fn new<P: AsRef<Path>>(path: P) -> std::io::Result<Chunk> {
        let file = File::open(path)?;
        let mapping = unsafe { Mmap::map(&file)? };
        Ok(Chunk { mapping })
    }
}
