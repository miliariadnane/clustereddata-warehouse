CREATE TABLE IF NOT EXISTS deals (
    id BIGSERIAL PRIMARY KEY,
    deal_unique_id VARCHAR(64) NOT NULL,
    from_currency_iso VARCHAR(3) NOT NULL,
    to_currency_iso VARCHAR(3) NOT NULL,
    deal_timestamp TIMESTAMPTZ NOT NULL,
    deal_amount NUMERIC(19,4) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_deals_unique_id ON deals (deal_unique_id);
CREATE INDEX IF NOT EXISTS idx_deals_timestamp ON deals (deal_timestamp);
