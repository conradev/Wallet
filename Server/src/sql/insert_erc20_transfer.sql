INSERT INTO token_transfer(block, [transaction], log, contract, [from], [to], value)
VALUES (?, ?, ?, ?, ?, ?, ?)
ON CONFLICT(block, [transaction], log) DO NOTHING;