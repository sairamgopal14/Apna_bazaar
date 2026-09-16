-- category exists so an admin can add a brand-new item category just by typing it
-- on the Excel sheet -- no code change, no redeploy, unlike offering_type/provider_type.

CREATE TABLE category (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE offering ADD COLUMN category_id UUID REFERENCES category(id);

CREATE INDEX idx_offering_category ON offering (category_id);
