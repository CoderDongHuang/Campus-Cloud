-- ============================================================
-- 校享云 (ShareCampus Cloud) - 数据库初始化脚本
-- Docker 启动 MySQL 时自动执行
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ==================== Nacos 配置库 ====================
CREATE DATABASE IF NOT EXISTS nacos_config
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- ==================== 业务数据库 ====================

-- 用户中心
CREATE DATABASE IF NOT EXISTS campus_user
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 商品服务
CREATE DATABASE IF NOT EXISTS campus_product
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 优惠券/营销
CREATE DATABASE IF NOT EXISTS campus_coupon
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 支付中心
CREATE DATABASE IF NOT EXISTS campus_payment
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 结算分账
CREATE DATABASE IF NOT EXISTS campus_settlement
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 通知中心
CREATE DATABASE IF NOT EXISTS campus_notify
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 评价反馈
CREATE DATABASE IF NOT EXISTS campus_review
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 文件中心
CREATE DATABASE IF NOT EXISTS campus_file
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 租户管理
CREATE DATABASE IF NOT EXISTS campus_tenant
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 系统管理（操作日志、数据字典等）
CREATE DATABASE IF NOT EXISTS campus_system
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- ==================== 订单分库（4库 × 16表 = 64张） ====================
CREATE DATABASE IF NOT EXISTS order_db0
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS order_db1
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS order_db2
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS order_db3
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
