USE campus_coupon;

CREATE TABLE IF NOT EXISTS coupon_template (
    id BIGINT NOT NULL,
    tenant_id BIGINT DEFAULT 0,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL COMMENT 'FULL_REDUCTION/DISCOUNT/DIRECT_REDUCTION',
    use_threshold DECIMAL(10,2) COMMENT '使用门槛',
    discount_value DECIMAL(10,2) NOT NULL,
    total_stock INT NOT NULL,
    grab_type VARCHAR(20) NOT NULL COMMENT 'GRAB/SEND/RECEIVE',
    grab_start_time DATETIME,
    grab_end_time DATETIME,
    user_limit INT DEFAULT 1,
    valid_days INT COMMENT '有效天数',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_tenant_grab (tenant_id, grab_type, grab_start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_coupon (
    id BIGINT NOT NULL,
    tenant_id BIGINT DEFAULT 0,
    template_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'UNUSED' COMMENT 'UNUSED/USED/EXPIRED',
    discount_value DECIMAL(10,2) NOT NULL,
    use_threshold DECIMAL(10,2),
    expire_time DATETIME NOT NULL,
    used_order_no VARCHAR(32),
    used_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_user_status (user_id, status),
    KEY idx_template (template_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
