-- ============================================================
-- 订单分库分表建表脚本
-- 4个数据库各16张 t_order + 16张 t_order_snapshot = 128张表
-- ============================================================

-- 在每个订单库执行
DELIMITER $$

CREATE PROCEDURE IF NOT EXISTS create_order_tables(IN db_name VARCHAR(50))
BEGIN
    DECLARE i INT DEFAULT 0;
    WHILE i < 16 DO
        SET @sql = CONCAT(
            'CREATE TABLE IF NOT EXISTS ', db_name, '.t_order_', i, ' (
              order_id BIGINT NOT NULL,
              tenant_id BIGINT NOT NULL DEFAULT 0,
              order_no VARCHAR(32) NOT NULL,
              user_id BIGINT NOT NULL,
              sku_id BIGINT NOT NULL,
              spu_id BIGINT NOT NULL,
              address_id BIGINT,
              status VARCHAR(20) NOT NULL DEFAULT ''PENDING_PAY'',
              original_amount DECIMAL(10,2),
              discount_amount DECIMAL(10,2) DEFAULT 0,
              actual_amount DECIMAL(10,2) NOT NULL,
              coupon_id BIGINT,
              worker_id BIGINT,
              appointment_time DATETIME,
              paid_time DATETIME,
              accepted_time DATETIME,
              complete_time DATETIME,
              cancel_time DATETIME,
              cancel_reason VARCHAR(500),
              version INT DEFAULT 0,
              create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
              update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
              PRIMARY KEY (order_id),
              KEY idx_user (tenant_id, user_id, status, create_time),
              KEY idx_worker (worker_id, status)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4'
        );
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;

        SET @sql = CONCAT(
            'CREATE TABLE IF NOT EXISTS ', db_name, '.t_order_snapshot_', i, ' (
              snapshot_id BIGINT NOT NULL,
              order_id BIGINT NOT NULL,
              order_no VARCHAR(32),
              snapshot_data JSON,
              create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
              PRIMARY KEY (snapshot_id),
              KEY idx_order (order_id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4'
        );
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;

        SET i = i + 1;
    END WHILE;
END$$

DELIMITER ;

-- 在 4 个库各执行一次
CALL create_order_tables('order_db0');
CALL create_order_tables('order_db1');
CALL create_order_tables('order_db2');
CALL create_order_tables('order_db3');

DROP PROCEDURE IF EXISTS create_order_tables;
