CREATE TABLE subscription_plans (
    plan_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    post_limit INTEGER,
    ai_matching_limit INTEGER,
    duration_days INTEGER DEFAULT 30,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

INSERT INTO subscription_plans (name, display_name, price, post_limit, ai_matching_limit) VALUES
('FREE', 'Gói Miễn Phí', 0, 1, 3),
('BASIC', 'Gói Cơ Bản', 199000, 15, 30),
('PRO', 'Gói Chuyên Nghiệp', 399000, NULL, 100);

CREATE TABLE user_subscriptions (
    subscription_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id),
    plan_id BIGINT NOT NULL REFERENCES subscription_plans(plan_id),
    posts_used INTEGER DEFAULT 0,
    ai_matching_used INTEGER DEFAULT 0,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    auto_renew BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE ai_matching_history (
    matching_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id),
    subscription_id BIGINT REFERENCES user_subscriptions(subscription_id),
    entity_type VARCHAR(20) NOT NULL,
    entity_id BIGINT,
    query TEXT,
    results JSONB,
    match_count INTEGER,
    execution_time_ms INTEGER,
    api_cost DECIMAL(8,4),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_user_subscriptions_user_status ON user_subscriptions(user_id, status);
CREATE INDEX idx_ai_matching_user_created ON ai_matching_history(user_id, created_at DESC);
