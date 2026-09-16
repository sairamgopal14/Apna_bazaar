-- Lets a rating carry an actual written word, not just a bare number.
ALTER TABLE order_slot ADD COLUMN review_text TEXT;
