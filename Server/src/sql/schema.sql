CREATE TABLE IF NOT EXISTS token_transfer (
    block INTEGER NOT NULL,
    [transaction] INTEGER NOT NULL,
    log INTEGER NOT NULL,
    contract BLOB NOT NULL CHECK(length([from]) == 20),
    [from] BLOB NOT NULL CHECK(length([from]) == 20),
    [to] BLOB NOT NULL CHECK(length([to]) == 20),
    value BLOB NOT NULL,

    PRIMARY KEY (block, [transaction], log)
) WITHOUT ROWID;

CREATE INDEX IF NOT EXISTS idx_toke_transfer_contract ON token_transfer(contract);
CREATE INDEX IF NOT EXISTS idx_toke_transfer_from ON token_transfer([from]);
CREATE INDEX IF NOT EXISTS idx_toke_transfer_to ON token_transfer([to]);
CREATE INDEX IF NOT EXISTS idx_toke_transfer_to_from ON token_transfer([to], [from]);
