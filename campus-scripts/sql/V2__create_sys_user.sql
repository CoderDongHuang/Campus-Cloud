-- ============================================================
-- 用户表 (campus_user 库)
-- ============================================================

USE campus_user;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL COMMENT '雪花ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    password VARCHAR(255) COMMENT '密码（BCrypt）',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像URL',
    user_type VARCHAR(20) NOT NULL DEFAULT 'STUDENT' COMMENT 'STUDENT/WORKER/ADMIN',
    status TINYINT DEFAULT 1 COMMENT '1正常 0禁用',
    last_login_time DATETIME COMMENT '最后登录时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0 COMMENT '0未删除 1已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_phone (phone),
    KEY idx_tenant_user (tenant_id, user_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
