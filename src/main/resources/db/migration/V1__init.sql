CREATE TABLE "order"
(
    order_id SERIAL PRIMARY KEY,
    status   VARCHAR(32),
    amount   NUMERIC(6, 2)
);