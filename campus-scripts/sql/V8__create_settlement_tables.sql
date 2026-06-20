USE campus_settlement;

CREATE TABLE IF NOT EXISTS settlement_order (
    id BIGINT NOT NULL, tenant_id BIGINT DEFAULT 0,
    order_id BIGINT NOT NULL, order_no VARCHAR(32) NOT NULL,
    user_id BIGINT NOT NULL COMMENT '师傅ID',
    total_amount DECIMAL(10,2) NOT NULL,
    platform_commission DECIMAL(10,2) NOT NULL,
    worker_amount DECIMAL(10,2) NOT NULL,
    commission_rate DECIMAL(5,4),
    status VARCHAR(20) NOT NULL DEFAULT 'WAIT_SETTLE',
    settled_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id), UNIQUE KEY uk_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS worker_wallet (
    user_id BIGINT NOT NULL,
    pending_amount DECIMAL(12,2) DEFAULT 0,
    available_amount DECIMAL(12,2) DEFAULT 0,
    frozen_amount DECIMAL(12,2) DEFAULT 0,
    withdrawn_amount DECIMAL(12,2) DEFAULT 0,
    total_earned DECIMAL(12,2) DEFAULT 0,
    version INT DEFAULT 0,
    PRIMARY KEY (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS withdraw_apply (
    id BIGINT NOT NULL, tenant_id BIGINT DEFAULT 0,
    withdraw_no VARCHAR(32) NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    channel VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    apply_time DATETIME,
    complete_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id), UNIQUE KEY uk_withdraw_no (withdraw_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS settlement_config (
    id BIGINT NOT NULL, tenant_id BIGINT DEFAULT 0,
    category_id BIGINT,
    platform_rate DECIMAL(5,4) NOT NULL DEFAULT 0.1500 COMMENT '平台抽佣比例',
    worker_rate DECIMAL(5,4) NOT NULL DEFAULT 0.8500 COMMENT '师傅到手比例',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
