-- Run this once on an existing MySQL/Aiven database if products are currently marked sold out.
UPDATE products
SET status = 'ACTIVE',
    is_sold_out = FALSE
WHERE status = 'SOLD_OUT'
   OR is_sold_out = TRUE;
