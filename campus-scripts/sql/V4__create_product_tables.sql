USE campus_product;

CREATE TABLE IF NOT EXISTS product_category (
    id BIGINT NOT NULL,
    tenant_id BIGINT DEFAULT 0,
    parent_id BIGINT DEFAULT 0,
    name VARCHAR(50) NOT NULL,
    sort_order INT DEFAULT 0,
    icon VARCHAR(255),
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_spu (
    id BIGINT NOT NULL,
    tenant_id BIGINT DEFAULT 0,
    category_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    main_image VARCHAR(255),
    status TINYINT DEFAULT 1,
    sales_count INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_category (category_id),
    FULLTEXT KEY ft_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_sku (
    id BIGINT NOT NULL,
    tenant_id BIGINT DEFAULT 0,
    spu_id BIGINT NOT NULL,
    name VARCHAR(100),
    price DECIMAL(10,2) NOT NULL,
    original_price DECIMAL(10,2),
    stock INT DEFAULT 9999,
    duration INT COMMENT '服务时长(分钟)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_spu (spu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
