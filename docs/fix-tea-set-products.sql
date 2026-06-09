-- Fix existing MySQL/Aiven data when Tea Set contains old or duplicated rows.
-- This keeps only the three original Tea Set products visible and repairs their images.

UPDATE products
   SET status = 'HIDDEN'
 WHERE category_id = (SELECT id FROM categories WHERE slug = 'tea-set')
   AND id NOT IN (15, 16, 18);

UPDATE products
   SET name = 'Rich Aroma LeafCup',
       summary = '단독구매상품',
       description = '단독구매상품',
       price = 25000,
       main_image_url = 'https://teehaus.co.kr/web/product/medium/202503/d06664755e02edd978e71168f7af3a66.jpg',
       search_keywords = 'Rich Aroma LeafCup Tea Set teehaus ronnefeldt',
       status = 'ACTIVE',
       is_sold_out = FALSE,
       display_order = 10
 WHERE id = 18;

UPDATE products
   SET name = 'Week Focus LeafCup',
       summary = '단독구매상품',
       description = '단독구매상품',
       price = 25000,
       main_image_url = 'https://teehaus.co.kr/web/product/medium/202503/46114c70e4865d5c89226c3b74ee436e.jpg',
       search_keywords = 'Week Focus LeafCup Tea Set teehaus ronnefeldt',
       status = 'ACTIVE',
       is_sold_out = FALSE,
       display_order = 20
 WHERE id = 16;

UPDATE products
   SET name = 'Healthy Week LeafCup',
       summary = '단독구매상품',
       description = '단독구매상품',
       price = 25000,
       main_image_url = 'https://teehaus.co.kr/web/product/medium/202503/1cb9e1581adf882dd9714037a5fe7e2a.jpg',
       search_keywords = 'Healthy Week LeafCup Tea Set teehaus ronnefeldt',
       status = 'ACTIVE',
       is_sold_out = FALSE,
       display_order = 30
 WHERE id = 15;
