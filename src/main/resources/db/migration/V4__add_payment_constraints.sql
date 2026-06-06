ALTER TABLE wallets
    ADD CONSTRAINT chk_wallet_balance_non_negative CHECK (balance >= 0);

ALTER TABLE transactions
    ADD CONSTRAINT chk_transaction_amount_positive CHECK (amount > 0);

ALTER TABLE transactions
    ADD CONSTRAINT chk_transaction_type_valid CHECK (type IN ('DEPOSIT', 'TRANSFER'));

ALTER TABLE transactions
    ADD CONSTRAINT chk_transaction_status_valid CHECK (status IN ('SUCCESS', 'FAILED'));
