-- Phase 5 pre-order schema.
-- order_slot is the first genuinely real "buyer intent" signal in this app -- the only
-- reason provider.rating can finally be earned instead of guessed (see Provider.java,
-- SearchResultCard.java comments dating back to Phase 1).

CREATE TABLE order_slot (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    offering_id         UUID NOT NULL REFERENCES offering(id),
    daily_line_item_id  UUID NOT NULL REFERENCES daily_line_item(id),
    provider_id         UUID NOT NULL REFERENCES provider(id),
    session_id          UUID NOT NULL,
    reserved_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    rating              SMALLINT
        CHECK (rating IS NULL OR (rating BETWEEN 1 AND 5)),
    rated_at            TIMESTAMPTZ
);

CREATE INDEX idx_order_slot_session ON order_slot (session_id);
CREATE INDEX idx_order_slot_provider_rating ON order_slot (provider_id, rating);
