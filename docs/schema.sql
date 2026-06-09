-- Ronnefeldt simple shopping mall schema
-- Target: MySQL 8.x / DBeaver
-- Run this file in the target database connection.

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS carts;
DROP TABLE IF EXISTS wishlists;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS inquiries;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS members;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE members (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(100) NULL,
    nickname VARCHAR(100) NULL,
    provider VARCHAR(30) NULL COMMENT 'LOCAL, NAVER, KAKAO, GOOGLE',
    provider_user_id VARCHAR(255) NULL,
    password_hash VARCHAR(255) NULL,
    grade VARCHAR(30) NOT NULL DEFAULT 'BRONZE' COMMENT 'BRONZE, SILVER, GOLD, MASTER',
    points INT NOT NULL DEFAULT 0,
    role VARCHAR(30) NOT NULL DEFAULT 'USER' COMMENT 'USER, ADMIN',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, WITHDRAWN, BLOCKED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_members_email (email),
    UNIQUE KEY uk_members_provider_user (provider, provider_user_id),
    KEY idx_members_grade (grade),
    KEY idx_members_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    parent_id BIGINT NULL,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(120) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_categories_slug (slug),
    KEY idx_categories_parent (parent_id),
    CONSTRAINT fk_categories_parent
        FOREIGN KEY (parent_id) REFERENCES categories(id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE products (
    id BIGINT NOT NULL AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    summary VARCHAR(500) NULL,
    description TEXT NULL,
    price INT NOT NULL,
    main_image_url VARCHAR(1000) NULL,
    detail_image_urls TEXT NULL COMMENT 'JSON or comma-separated URLs for the initial version',
    search_keywords TEXT NULL COMMENT 'Integrated search keywords such as black tea, oolong, leafcup',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, HIDDEN, SOLD_OUT',
    is_sold_out BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_products_category (category_id),
    KEY idx_products_status (status),
    KEY idx_products_sold_out (is_sold_out),
    FULLTEXT KEY ft_products_search (name, summary, description, search_keywords),
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id) REFERENCES categories(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE carts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_carts_member (member_id),
    CONSTRAINT fk_carts_member
        FOREIGN KEY (member_id) REFERENCES members(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE cart_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price INT NOT NULL,
    delivery_type VARCHAR(30) NOT NULL DEFAULT 'DOMESTIC' COMMENT 'DOMESTIC, OVERSEAS',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cart_items_cart_product_delivery (cart_id, product_id, delivery_type),
    KEY idx_cart_items_product (product_id),
    CONSTRAINT fk_cart_items_cart
        FOREIGN KEY (cart_id) REFERENCES carts(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_product
        FOREIGN KEY (product_id) REFERENCES products(id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_cart_items_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE wishlists (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_wishlists_member_product (member_id, product_id),
    KEY idx_wishlists_product (product_id),
    CONSTRAINT fk_wishlists_member
        FOREIGN KEY (member_id) REFERENCES members(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_wishlists_product
        FOREIGN KEY (product_id) REFERENCES products(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    order_no VARCHAR(60) NOT NULL,
    order_status VARCHAR(30) NOT NULL DEFAULT 'CREATED' COMMENT 'CREATED, PAID, PREPARING, SHIPPING, DELIVERED, CANCELED, REFUNDED',
    total_amount INT NOT NULL DEFAULT 0,
    receiver_name VARCHAR(100) NULL,
    receiver_phone VARCHAR(30) NULL,
    postal_code VARCHAR(20) NULL,
    address1 VARCHAR(255) NULL,
    address2 VARCHAR(255) NULL,
    delivery_memo VARCHAR(255) NULL,
    delivery_status VARCHAR(30) NOT NULL DEFAULT 'READY' COMMENT 'READY, SHIPPING, DELIVERED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_orders_order_no (order_no),
    KEY idx_orders_member (member_id),
    KEY idx_orders_status (order_status),
    CONSTRAINT fk_orders_member
        FOREIGN KEY (member_id) REFERENCES members(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE order_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    cart_item_id BIGINT NULL COMMENT 'Original cart item id used to remove paid basket items',
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    quantity INT NOT NULL,
    unit_price INT NOT NULL,
    total_price INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_order_items_order (order_id),
    KEY idx_order_items_cart_item (cart_item_id),
    KEY idx_order_items_product (product_id),
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id) REFERENCES products(id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_order_items_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE payments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    provider VARCHAR(50) NOT NULL COMMENT 'TOSS, NAVER_PAY, KAKAO_PAY, etc',
    payment_key VARCHAR(255) NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'READY' COMMENT 'READY, DONE, FAILED, CANCELED',
    method VARCHAR(50) NULL,
    amount INT NOT NULL,
    approved_at DATETIME NULL,
    canceled_at DATETIME NULL,
    raw_response JSON NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payments_order (order_id),
    KEY idx_payments_status (status),
    CONSTRAINT fk_payments_order
        FOREIGN KEY (order_id) REFERENCES orders(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE reviews (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    rating INT NOT NULL DEFAULT 5,
    content TEXT NOT NULL,
    image_url VARCHAR(1000) NULL,
    is_visible BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_reviews_member (member_id),
    KEY idx_reviews_product (product_id),
    CONSTRAINT fk_reviews_member
        FOREIGN KEY (member_id) REFERENCES members(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_reviews_product
        FOREIGN KEY (product_id) REFERENCES products(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE inquiries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    product_id BIGINT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    answer TEXT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN, ANSWERED, CLOSED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    answered_at DATETIME NULL,
    PRIMARY KEY (id),
    KEY idx_inquiries_member (member_id),
    KEY idx_inquiries_product (product_id),
    KEY idx_inquiries_status (status),
    CONSTRAINT fk_inquiries_member
        FOREIGN KEY (member_id) REFERENCES members(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_inquiries_product
        FOREIGN KEY (product_id) REFERENCES products(id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed data for initial development
INSERT INTO categories (id, parent_id, name, slug, display_order) VALUES
    (1, NULL, 'Tea Set', 'tea-set', 10),
    (2, NULL, 'Loose Tea', 'loose-tea', 20),
    (3, NULL, 'Tea-Caddy', 'tea-caddy', 30),
    (4, NULL, 'LeafCup', 'leafcup', 40),
    (5, NULL, 'Teavelope', 'teavelope', 50),
    (6, NULL, 'Tea Ware', 'tea-ware', 60),
    (7, NULL, 'Life Style', 'life-style', 70);

INSERT INTO products
    (id, category_id, name, summary, description, price, main_image_url, search_keywords, status, is_sold_out, display_order)
VALUES
    (1, 1, 'Rich Aroma LeafCup', '단독구매상품', '깊고 풍부한 아로마를 담은 LeafCup 티 세트입니다.', 25000,
     'https://teehaus.co.kr/web/product/big/202503/1ae4f5970b2537b3dd57f97ac423e812.jpg',
     'tea set leafcup rich aroma 홍차 티세트', 'ACTIVE', FALSE, 10),
    (2, 1, 'Week Focus LeafCup', '단독구매상품', '집중이 필요한 한 주를 위해 구성된 LeafCup 티 세트입니다.', 25000,
     'https://teehaus.co.kr/web/product/big/202503/dcfe96872a82ba911970d3514ad51b2b.jpg',
     'tea set leafcup week focus 홍차 티세트', 'ACTIVE', FALSE, 20),
    (3, 1, 'Healthy Week LeafCup', '단독구매상품', '건강한 일상을 위한 LeafCup 티 세트입니다.', 25000,
     'https://teehaus.co.kr/web/product/big/202503/5e668feba8e3727c596c5bacff1777e2.jpg',
     'tea set leafcup healthy week 허브차 티세트', 'ACTIVE', FALSE, 30);
-- Optional local test member
INSERT INTO members
    (id, email, name, nickname, provider, provider_user_id, grade, points, role, status)
VALUES
    (1, 'demo@ronnefeldt.local', 'Demo User', 'demo', 'LOCAL', 'demo-user', 'BRONZE', 0, 'USER', 'ACTIVE');

INSERT INTO carts (id, member_id) VALUES (1, 1);

