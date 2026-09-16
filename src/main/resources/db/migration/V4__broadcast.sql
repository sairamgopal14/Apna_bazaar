-- The broadcast table. Every broadcast gets created for real (organic,
-- AI-generated, or sponsored) and delivered by a scheduled job.
-- searches_triggered/whatsapp_taps are logged manually -- there's no real
-- WhatsApp delivery integration to auto-track them, so the admin reports
-- what they personally observed afterward.

CREATE TABLE broadcast (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    community_id        UUID NOT NULL REFERENCES community(id),
    provider_id         UUID REFERENCES provider(id),
    message             TEXT NOT NULL,
    broadcast_type      VARCHAR(20) NOT NULL
        CHECK (broadcast_type IN ('organic', 'ai_generated', 'sponsored')),
    status              VARCHAR(20) NOT NULL
        CHECK (status IN ('draft', 'scheduled', 'sent', 'recurring_active', 'recurring_paused')),
    scheduled_at        TIMESTAMPTZ,
    recurrence_pattern  VARCHAR(20)
        CHECK (recurrence_pattern IN ('daily', 'weekly', 'weekdays')),
    last_sent_at        TIMESTAMPTZ,
    promo_amount        INT,
    searches_triggered  INT NOT NULL DEFAULT 0,
    whatsapp_taps       INT NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_broadcast_status ON broadcast (status);
