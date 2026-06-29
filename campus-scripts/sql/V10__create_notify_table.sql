USE campus_notify;

CREATE TABLE IF NOT EXISTS message_record (
    id BIGINT NOT NULL, tenant_id BIGINT DEFAULT 0,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) COMMENT 'SMS/PUSH/INBOX',
    title VARCHAR(200),
    content TEXT,
    template_code VARCHAR(50),
    status TINYINT DEFAULT 0 COMMENT '0待发送 1成功 2失败',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id), KEY idx_user (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
