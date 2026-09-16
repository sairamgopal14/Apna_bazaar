-- Core schema: communities, sellers, their catalog, and today's specific listings.
-- offering_schedule and daily_post are added later once Excel ingestion needs them;
-- daily_line_item carries its own window/timing fields directly until then.
-- category is intentionally omitted here: provider_type / offering_type enums cover it for now.

CREATE TABLE community (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(200) NOT NULL,
    slug        VARCHAR(100) NOT NULL UNIQUE,
    total_flats INT,
    city        VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE provider (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    community_id     UUID NOT NULL REFERENCES community(id),
    name             VARCHAR(200) NOT NULL,
    shop_name        VARCHAR(200) NOT NULL,
    flat_number      VARCHAR(50),
    whatsapp_number  VARCHAR(20) NOT NULL,
    provider_type    VARCHAR(20) NOT NULL
        CHECK (provider_type IN ('food_seller', 'grocery', 'service', 'tuition', 'other')),
    status           VARCHAR(20) NOT NULL DEFAULT 'pending'
        CHECK (status IN ('active', 'pending', 'suspended', 'inactive')),
    rating           NUMERIC(3, 2),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (community_id, whatsapp_number)
);

CREATE INDEX idx_provider_community_status ON provider (community_id, status);

CREATE TABLE offering (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider_id    UUID NOT NULL REFERENCES provider(id),
    name           VARCHAR(200) NOT NULL,
    description    TEXT NOT NULL,
    base_price     NUMERIC(10, 2),
    offering_type  VARCHAR(20) NOT NULL
        CHECK (offering_type IN ('food_item', 'grocery_item', 'service_package', 'class_', 'other')),
    is_available   BOOLEAN NOT NULL DEFAULT true,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_offering_provider ON offering (provider_id);

CREATE TABLE daily_line_item (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    offering_id             UUID NOT NULL REFERENCES offering(id),
    item_date               DATE NOT NULL,
    item_name               VARCHAR(200) NOT NULL,
    price                   NUMERIC(10, 2) NOT NULL,
    delivery_type           VARCHAR(20) NOT NULL DEFAULT 'pickup'
        CHECK (delivery_type IN ('pickup', 'home_delivery', 'both')),
    serves_from             TIME,
    serves_to               TIME,
    accepts_realtime        BOOLEAN NOT NULL DEFAULT true,
    realtime_cutoff_minutes INT,
    preorder_required       BOOLEAN NOT NULL DEFAULT false,
    preorder_closes_at      TIME,
    preorder_day_offset     SMALLINT NOT NULL DEFAULT 0,
    is_available            BOOLEAN NOT NULL DEFAULT true,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (offering_id, item_date)
);

CREATE INDEX idx_daily_line_item_offering_date ON daily_line_item (offering_id, item_date);

CREATE TABLE search_event (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    community_id      UUID NOT NULL REFERENCES community(id),
    raw_query         TEXT NOT NULL,
    normalised_query  TEXT NOT NULL,
    result_count      INT NOT NULL,
    had_results       BOOLEAN NOT NULL,
    query_date        DATE NOT NULL,
    time_bucket       VARCHAR(20) NOT NULL,
    day_of_week       SMALLINT NOT NULL,
    session_id        UUID,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_search_event_community_date ON search_event (community_id, query_date);

CREATE TABLE zero_result_log (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    community_id      UUID NOT NULL REFERENCES community(id),
    normalised_query  TEXT NOT NULL,
    occurrence_count  INT NOT NULL DEFAULT 1,
    first_seen        DATE NOT NULL,
    last_seen         DATE NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'new_'
        CHECK (status IN ('new_', 'reviewing', 'provider_sought', 'fulfilled', 'declined')),
    UNIQUE (community_id, normalised_query)
);

CREATE INDEX idx_zero_result_log_community_status ON zero_result_log (community_id, status);
