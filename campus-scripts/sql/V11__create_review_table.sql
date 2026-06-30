USE campus_review;

CREATE TABLE IF NOT EXISTS order_review (
    id BIGINT NOT NULL, tenant_id BIGINT DEFAULT 0,
    order_id BIGINT NOT NULL, user_id BIGINT NOT NULL, worker_id BIGINT NOT NULL,
    rating TINYINT NOT NULL COMMENT '评分1-5',
    content VARCHAR(500), images VARCHAR(1000),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id), UNIQUE KEY uk_order (order_id), KEY idx_worker (worker_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
