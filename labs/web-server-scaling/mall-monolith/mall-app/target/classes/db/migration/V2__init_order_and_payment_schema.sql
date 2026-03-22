CREATE TABLE ord_order
(
    id           BIGSERIAL PRIMARY KEY,
    order_no     VARCHAR(64)    NOT NULL UNIQUE,
    status       VARCHAR(32)    NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ord_order_item
(
    id            BIGSERIAL PRIMARY KEY,
    order_no      VARCHAR(64)    NOT NULL,
    product_id    BIGINT         NOT NULL,
    product_name  VARCHAR(200)   NOT NULL,
    product_price DECIMAL(10, 2) NOT NULL,
    quantity      INT            NOT NULL,
    item_amount   DECIMAL(10, 2) NOT NULL,
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ord_order_item_order_no ON ord_order_item (order_no);

CREATE TABLE pay_payment
(
    id          BIGSERIAL PRIMARY KEY,
    payment_no  VARCHAR(64)    NOT NULL UNIQUE,
    order_no    VARCHAR(64)    NOT NULL UNIQUE,
    status      VARCHAR(32)    NOT NULL,
    amount      DECIMAL(10, 2) NOT NULL,
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
