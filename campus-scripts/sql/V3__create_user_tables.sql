USE campus_user;

CREATE TABLE IF NOT EXISTS user_address (
    id BIGINT NOT NULL,
    tenant_id BIGINT DEFAULT 0,
    user_id BIGINT NOT NULL,
    contact_name VARCHAR(50),
    contact_phone VARCHAR(20),
    campus_name VARCHAR(100) COMMENT '校区',
    building_name VARCHAR(100) COMMENT '楼栋',
    room_number VARCHAR(50) COMMENT '房间号',
    is_default TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户地址';

CREATE TABLE IF NOT EXISTS worker_certification (
    id BIGINT NOT NULL,
    tenant_id BIGINT DEFAULT 0,
    user_id BIGINT NOT NULL,
    real_name VARCHAR(50),
    id_card VARCHAR(18),
    id_card_front VARCHAR(255) COMMENT '身份证正面',
    id_card_back VARCHAR(255) COMMENT '身份证反面',
    skill_certificate VARCHAR(255) COMMENT '技能证书',
    skills VARCHAR(500) COMMENT '技能标签JSON',
    status TINYINT DEFAULT 0 COMMENT '0待审核 1通过 2驳回',
    audit_remark VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='师傅认证';
