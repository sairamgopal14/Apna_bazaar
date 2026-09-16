-- Phase 1 manual seed: two real communities, a handful of active sellers with rich
-- catalog descriptions. daily_line_item rows (today's price/window) are NOT seeded
-- here since Flyway migrations run once — DevDataSeeder creates/refreshes those on
-- every app startup so `today` always has data, whichever day you actually run this.

INSERT INTO community (id, name, slug, total_flats, city) VALUES
    ('11111111-1111-1111-1111-111111111111', 'MyHome Tridasa', 'tridasa', 2700, 'Hyderabad'),
    ('22222222-2222-2222-2222-222222222222', 'MyHome Sayuk', 'sayuk', 3800, 'Hyderabad');

INSERT INTO provider (id, community_id, name, shop_name, flat_number, whatsapp_number, provider_type, status) VALUES
    ('a1111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', 'Lakshmi Reddy', 'Lakshmi''s Kitchen', 'T4-2205', '+919876543210', 'food_seller', 'active'),
    ('a2222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 'Sunita Rao', 'Sunita''s Lunch Home', 'A2-1103', '+919876543211', 'food_seller', 'active'),
    ('a3333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111', 'Meena Iyer', 'Meena''s Fresh Juice Bar', 'B1-0507', '+919876543212', 'food_seller', 'active'),
    ('a4444444-4444-4444-4444-444444444444', '11111111-1111-1111-1111-111111111111', 'Rahul Sharma', 'Rahul''s Frozen Bites', 'C3-1809', '+919876543213', 'food_seller', 'active'),
    ('a5555555-5555-5555-5555-555555555555', '11111111-1111-1111-1111-111111111111', 'Akku Reddy', 'Healthy Rasoi by Akku''s Kitchen', 'T4-1902', '+919876543214', 'food_seller', 'active'),
    ('a6666666-6666-6666-6666-666666666666', '11111111-1111-1111-1111-111111111111', 'Fresh Mart', 'Fresh Mart Grocery', 'G1-0001', '+919876543215', 'grocery', 'active'),
    ('a7777777-7777-7777-7777-777777777777', '11111111-1111-1111-1111-111111111111', 'Priya Nair', 'Priya''s Maths Tuition', 'D5-1204', '+919876543216', 'tuition', 'active');

INSERT INTO offering (provider_id, name, description, base_price, offering_type) VALUES
    ('a1111111-1111-1111-1111-111111111111', 'Idli Dosa Batter', 'Fresh idli and dosa batter, ground daily from soaked rice and urad dal. 1kg pack, ready to use.', 80, 'food_item'),
    ('a2222222-2222-2222-2222-222222222222', 'Home-style Andhra Lunch Thali', 'Full lunch thali with rice, sambar, rasam, two vegetable curries, curd and pickle. Home-cooked South Indian food.', 120, 'food_item'),
    ('a3333333-3333-3333-3333-333333333333', 'Fresh Fruit Juice', 'Freshly squeezed seasonal fruit juices - watermelon, orange, pomegranate, mixed fruit. No added sugar.', 60, 'food_item'),
    ('a4444444-4444-4444-4444-444444444444', 'Frozen Snacks Combo', 'Ready-to-fry frozen snacks - samosa, spring rolls, chicken kebabs. Freezer-stable, always in stock.', 150, 'food_item'),
    ('a5555555-5555-5555-5555-555555555555', 'Healthy Millet Tiffin', 'Millet-based breakfast tiffin - idli, dosa, upma made with foxtail and pearl millet for a healthier start.', 90, 'food_item'),
    ('a6666666-6666-6666-6666-666666666666', 'Daily Grocery Essentials', 'Milk, bread, eggs and fresh vegetables delivered same day. Basic kitchen staples for the week.', NULL, 'grocery_item'),
    ('a7777777-7777-7777-7777-777777777777', 'Class 9-10 Maths Tuition', 'One-on-one and small group maths tuition for CBSE class 9 and 10 students, evening slots.', 2000, 'service_package');
