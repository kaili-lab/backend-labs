INSERT INTO products (id, name, description) VALUES
    (1, '商品-1', '数据库版商品详情');

INSERT INTO inventories (product_id, available) VALUES
    (1, 120);

INSERT INTO recommendations (product_id, summary) VALUES
    (1, '数据库推荐：商品-2、商品-3、商品-4');
