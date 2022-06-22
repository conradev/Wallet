use crate::log::Log;
use rlp::{Decodable, DecoderError, Rlp};

#[derive(Debug)]
pub struct Receipt {
    pub logs: Vec<Log>,
}

impl Decodable for Receipt {
    fn decode(rlp: &Rlp) -> Result<Self, DecoderError> {
        let logs_rlp = rlp.at(2)?;
        let logs_result = logs_rlp
            .iter()
            .map(|r| Log::decode(&r))
            .filter(|r| r.as_ref().ok().map(|l| l.is_known()).unwrap_or(true));
        let logs = Result::from_iter(logs_result)?;
        Ok(Receipt { logs })
    }
}
