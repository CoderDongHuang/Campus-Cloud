USE campus_tenant;

CREATE TABLE IF NOT EXISTS tenant (
    id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL COMMENT '学校全称',
    short_name VARCHAR(50),
    logo VARCHAR(255),
    contact_name VARCHAR(50),
    contact_phone VARCHAR(20),
    package_id BIGINT,
    status TINYINT DEFAULT 0 COMMENT '0待审核 1正式 2试用 3欠费 4关闭',
    expire_time DATE,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id), KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
