-- Database Creation (if running manually in MySQL Workbench or CLI)
CREATE DATABASE IF NOT EXISTS wallet_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE wallet_system;

-- Table: users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    balance DECIMAL(19, 4) DEFAULT 0.0000,
    role VARCHAR(50) DEFAULT 'USER',
    INDEX idx_user_email (email)
) ENGINE=InnoDB;

-- Table: wallet
CREATE TABLE IF NOT EXISTS wallet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_name VARCHAR(255) NOT NULL,
    balance DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    user_id BIGINT,
    INDEX idx_wallet_user_id (user_id),
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Table: transactions
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    from_wallet_id BIGINT,
    to_wallet_id BIGINT,
    amount DECIMAL(19, 4) NOT NULL,
    status VARCHAR(50) NOT NULL,
    transaction_time DATETIME(6),
    idempotency_key VARCHAR(255) UNIQUE,
    INDEX idx_tx_from_wallet (from_wallet_id),
    INDEX idx_tx_to_wallet (to_wallet_id),
    INDEX idx_tx_idempotency (idempotency_key)
) ENGINE=InnoDB;

-- Table: ledger_entry
CREATE TABLE IF NOT EXISTS ledger_entry (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    wallet_id BIGINT NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    type VARCHAR(20) NOT NULL, -- CREDIT or DEBIT
    description VARCHAR(255),
    transaction_id BIGINT,
    transaction_time DATETIME(6),
    INDEX idx_ledger_wallet (wallet_id),
    INDEX idx_ledger_type (type),
    INDEX idx_ledger_tx_id (transaction_id),
    CONSTRAINT fk_ledger_wallet FOREIGN KEY (wallet_id) REFERENCES wallet(id) ON DELETE CASCADE
) ENGINE=InnoDB;
