CREATE TABLE products (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL
);

CREATE TABLE inventories (
    product_id BIGINT PRIMARY KEY,
    available INTEGER NOT NULL
);

CREATE TABLE recommendations (
    product_id BIGINT PRIMARY KEY,
    summary VARCHAR(255) NOT NULL
);
