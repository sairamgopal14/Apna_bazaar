-- Dev-only convenience: forces every seller's TODAY listing to show as
-- "Serving now" regardless of their real morning/evening windows, so you can
-- search-test any item at any hour without waiting for its actual time slot.
--
-- This does NOT touch the seed patterns or any code -- it's a live edit to
-- today's daily_line_item rows only. DevDataSeeder creates a fresh row with
-- the REAL pattern the next time the app starts on a new date, so this
-- override is naturally temporary: good for tonight only, safe to re-run
-- every time you want to test late.
--
-- Run with:
--   "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -h localhost -d apna_bazaar -f scripts/open-all-for-testing.sql

UPDATE daily_line_item
SET serves_from = '00:00:00',
    serves_to = '23:59:59',
    accepts_realtime = true,
    realtime_cutoff_minutes = NULL
WHERE item_date = CURRENT_DATE;
