USE campus_payment;

CREATE TABLE IF NOT EXISTS pay_order (
    id BIGINT NOT NULL,
    tenant_id BIGINT DEFAULT 0,
    pay_order_no VARCHAR(32) NOT NULL,
    order_id BIGINT NOT NULL,
    order_no VARCHAR(32) NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    channel VARCHAR(20),
    channel_trade_no VARCHAR(64),
    status VARCHAR(20) NOT NULL DEFAULT 'WAIT_PAY',
    pay_time DATETIME,
    expire_time DATETIME,
    callback_data TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pay_order_no (pay_order_no),
    KEY idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS pay_refund (
    id BIGINT NOT NULL,
    tenant_id BIGINT DEFAULT 0,
    refund_no VARCHAR(32) NOT NULL,
    pay_order_no VARCHAR(32) NOT NULL,
    order_no VARCHAR(32) NOT NULL,
    refund_amount DECIMAL(10,2) NOT NULL,
    reason VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'REFUNDING',
    channel_refund_no VARCHAR(64),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_refund_no (refund_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
