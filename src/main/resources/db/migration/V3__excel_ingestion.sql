-- Phase 2: Excel ingestion introduces the two tables deferred from Phase 1.
--
-- offering_schedule: a seller's recurring weekly default timing (one row per
-- offering). The admin's daily sheet can leave timing columns blank when
-- nothing changed; ingestion fills the gap from here instead of forcing a
-- retype every morning.
--
-- daily_post: a header row per provider per day, tracking where a day's
-- listings came from (excel_upload | whatsapp_manual | app_post). Every
-- daily_line_item created from here on links back to one.

CREATE TABLE offering_schedule (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    offering_id             UUID NOT NULL UNIQUE REFERENCES offering(id),
    delivery_type           VARCHAR(20) NOT NULL DEFAULT 'pickup'
        CHECK (delivery_type IN ('pickup', 'home_delivery', 'both')),
    serves_from             TIME,
    serves_to               TIME,
    accepts_realtime        BOOLEAN NOT NULL DEFAULT true,
    realtime_cutoff_minutes INT,
    preorder_required       BOOLEAN NOT NULL DEFAULT false,
    preorder_closes_at      TIME,
    preorder_day_offset     SMALLINT NOT NULL DEFAULT 0,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE daily_post (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider_id  UUID NOT NULL REFERENCES provider(id),
    post_date    DATE NOT NULL,
    source       VARCHAR(20) NOT NULL
        CHECK (source IN ('excel_upload', 'whatsapp_manual', 'app_post')),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (provider_id, post_date)
);

ALTER TABLE daily_line_item
    ADD COLUMN daily_post_id UUID REFERENCES daily_post(id);

CREATE INDEX idx_daily_line_item_daily_post ON daily_line_item (daily_post_id);
