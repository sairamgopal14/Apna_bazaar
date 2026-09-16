-- Phase 4 analytics schema.
-- offering_impression/offering_click give real (if low-volume) click-through-rate data,
-- instead of faking it the way Phase 3 avoided faking broadcast performance.
-- demand_insight stores the weekly AI-written summary of search activity per community.

CREATE TABLE offering_impression (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    community_id  UUID NOT NULL REFERENCES community(id),
    provider_id   UUID NOT NULL REFERENCES provider(id),
    offering_id   UUID NOT NULL REFERENCES offering(id),
    shown_on      DATE NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_offering_impression_provider_date ON offering_impression (provider_id, shown_on);

CREATE TABLE offering_click (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    community_id  UUID NOT NULL REFERENCES community(id),
    provider_id   UUID NOT NULL REFERENCES provider(id),
    offering_id   UUID NOT NULL REFERENCES offering(id),
    clicked_on    DATE NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_offering_click_provider_date ON offering_click (provider_id, clicked_on);

CREATE TABLE demand_insight (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    community_id  UUID NOT NULL REFERENCES community(id),
    week_start    DATE NOT NULL,
    insight_text  TEXT NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (community_id, week_start)
);
