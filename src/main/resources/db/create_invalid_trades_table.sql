-- Create rttm_invalid_trades table
CREATE TABLE IF NOT EXISTS rttm_invalid_trades (
    id BIGSERIAL PRIMARY KEY,
    trade_id UUID NOT NULL,
    portfolio_id UUID NOT NULL,
    symbol VARCHAR(16) NOT NULL,
    side VARCHAR(8) NOT NULL,
    price_per_stock DECIMAL(19, 4),
    quantity BIGINT,
    trade_timestamp TIMESTAMP NOT NULL,
    validation_errors TEXT NOT NULL,
    event_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_invalid_time ON rttm_invalid_trades(event_time);
CREATE INDEX IF NOT EXISTS idx_invalid_trade_id ON rttm_invalid_trades(trade_id);

-- Add comment
COMMENT ON TABLE rttm_invalid_trades IS 'Stores invalid trade events from validation service for monitoring and metrics';
