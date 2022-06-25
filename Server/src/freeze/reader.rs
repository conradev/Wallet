use super::index::Index;
use crate::freeze::index::Region;
use crate::log::Log;
use crate::Block;
use log::{error, info};
use memmap::Mmap;
use rayon::iter::{
    FromParallelIterator, IndexedParallelIterator, IntoParallelIterator, IntoParallelRefIterator,
    ParallelBridge, ParallelIterator,
};
use rayon::prelude::ParallelSlice;
use rlp::{Decodable, Rlp};
use snap::raw::Decoder;
use std::cmp::Ordering;
use std::fs::File;
use std::io;
use std::ops::Range;
use std::path::{Iter, Path, PathBuf};

pub trait Thawable: Send {
    fn thaw_from(block: Block, buf: Vec<u8>) -> Self;
}

pub struct Reader {
    mappings: Vec<Mmap>,
    regions: Vec<Region>,
}

impl Reader {
    pub fn new<P: AsRef<Path>, T: AsRef<str>>(directory: P, base_name: T) -> io::Result<Reader> {
        let mut index_path = directory.as_ref().to_path_buf();
        index_path.push(base_name.as_ref());
        index_path.set_extension("cidx");
        let index = Index::new(index_path)?;

        let files: Vec<File> = Result::from_iter(FileEnumerator::new(
            directory.as_ref().to_path_buf(),
            base_name.as_ref().to_string(),
        ))?;
        let mappings: Vec<Mmap> = Result::from_iter(files.iter().map(|f| unsafe { Mmap::map(f) }))?;
        let file_lens: Vec<_> = Result::from_par_iter(
            files
                .par_iter()
                .map(|f| f.metadata().map(|m| m.len() as u32)),
        )?;
        let regions = index.load(file_lens);
        Ok(Reader { mappings, regions })
    }

    pub fn read<T: Thawable>(self) -> impl Iterator<Item = Vec<T>> {
        let mut offsets: Vec<_> = self
            .regions
            .par_windows(2)
            .filter_map(|window| {
                if (window[0].file != window[1].file) {
                    Some(window[1].block as usize)
                } else {
                    None
                }
            })
            .collect();
        offsets.insert(0, 0);

        let ranges: Vec<Range<usize>> = offsets
            .par_windows(2)
            .map(move |window| (window[0]..window[1]))
            .collect();

        ranges.into_iter().map(move |range| {
            self.regions[range]
                .into_par_iter()
                .filter_map(|region| {
                    if region.len < 4 {
                        return None;
                    }

                    let start = region.offset as usize;
                    let end = start + region.len as usize;
                    let compressed = &self.mappings[region.file as usize][start..end];

                    let mut decoder = Decoder::default();

                    match decoder.decompress_vec(compressed) {
                        Ok(buf) => Some(T::thaw_from(region.block, buf)),
                        Err(e) => {
                            error!("Failed to decompressed RLP data: {:?}", e);
                            None
                        }
                    }
                })
                .collect()
        })
    }
}

struct FileEnumerator {
    index: usize,
    directory: PathBuf,
    base_name: String,
}

impl FileEnumerator {
    fn new(directory: PathBuf, base_name: String) -> FileEnumerator {
        FileEnumerator {
            index: 0,
            directory,
            base_name,
        }
    }
}

impl Iterator for FileEnumerator {
    type Item = std::io::Result<File>;

    fn next(&mut self) -> Option<Self::Item> {
        let mut path = self.directory.clone();
        path.push(format!("{}.{:04}.cdat", self.base_name, self.index));
        self.index += 1;

        match File::open(path) {
            Ok(f) => Some(Ok(f)),
            Err(ref e) if e.kind() == std::io::ErrorKind::NotFound => None,
            Err(e) => Some(Err(e)),
        }
    }
}
