CREATE TABLE cat_category
(
    id           BIGSERIAL PRIMARY KEY,
    category_id  BIGINT       NOT NULL UNIQUE,
    name         VARCHAR(100) NOT NULL,
    status       VARCHAR(32)  NOT NULL,
    sort_order   INT          NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE prd_product
(
    id           BIGSERIAL PRIMARY KEY,
    product_id    BIGINT         NOT NULL UNIQUE,
    category_id   BIGINT         NOT NULL,
    name          VARCHAR(200)   NOT NULL,
    price         DECIMAL(10, 2) NOT NULL,
    status        VARCHAR(32)    NOT NULL,
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE inv_inventory
(
    id           BIGSERIAL PRIMARY KEY,
    product_id    BIGINT      NOT NULL UNIQUE,
    stock         INT         NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO cat_category (category_id, name, status, sort_order)
VALUES (1001, '精品茶叶', 'ENABLED', 10),
       (1002, '茶具周边', 'ENABLED', 20);

INSERT INTO prd_product (product_id, category_id, name, price, status)
VALUES (2001, 1001, '金骏眉 50g', 128.00, 'ON_SHELF'),
       (2002, 1001, '正山小种 50g', 98.00, 'ON_SHELF'),
       (2003, 1002, '白瓷盖碗', 68.00, 'OFF_SHELF');

INSERT INTO inv_inventory (product_id, stock)
VALUES (2001, 120),
       (2002, 86),
       (2003, 35);
