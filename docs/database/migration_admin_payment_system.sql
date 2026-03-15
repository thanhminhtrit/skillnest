-- Migration: Add Admin/Manager roles and payment escrow system
-- Date: 2026-03-09

-- Step 1: Add ADMIN and MANAGER roles
INSERT INTO roles (name) VALUES ('ADMIN') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('MANAGER') ON CONFLICT (name) DO NOTHING;

-- Step 2: Add BANNED status support (already added to enum)
-- No schema change needed, just enum update in code

-- Step 3: Create payment_requests table
CREATE TABLE IF NOT EXISTS payment_requests (
    payment_request_id BIGSERIAL PRIMARY KEY,
    proposal_id BIGINT NOT NULL UNIQUE,
    client_id BIGINT NOT NULL,
    total_amount DECIMAL(12, 2) NOT NULL,
    platform_fee DECIMAL(12, 2) NOT NULL,
    student_amount DECIMAL(12, 2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_PAYMENT',
    qr_code_url VARCHAR(500),
    payment_reference VARCHAR(50) NOT NULL UNIQUE,
    bank_transfer_note TEXT,
    verified_by BIGINT,
    verified_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_proposal FOREIGN KEY (proposal_id) REFERENCES proposals(proposal_id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_client FOREIGN KEY (client_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_verifier FOREIGN KEY (verified_by) REFERENCES users(user_id) ON DELETE SET NULL
);

-- Step 4: Create transactions table
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT,
    from_user_id BIGINT,
    to_user_id BIGINT,
    type VARCHAR(30) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transaction_contract FOREIGN KEY (contract_id) REFERENCES contracts(contract_id) ON DELETE CASCADE,
    CONSTRAINT fk_transaction_from_user FOREIGN KEY (from_user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT fk_transaction_to_user FOREIGN KEY (to_user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- Step 5: Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_payment_reference ON payment_requests(payment_reference);
CREATE INDEX IF NOT EXISTS idx_payment_status ON payment_requests(status);
CREATE INDEX IF NOT EXISTS idx_payment_created ON payment_requests(created_at);
CREATE INDEX IF NOT EXISTS idx_transaction_contract ON transactions(contract_id);
CREATE INDEX IF NOT EXISTS idx_transaction_type ON transactions(type);
CREATE INDEX IF NOT EXISTS idx_transaction_created ON transactions(created_at);

-- Step 6: Add comments for documentation
COMMENT ON TABLE payment_requests IS 'Escrow payment requests with 8% platform fee';
COMMENT ON TABLE transactions IS 'Financial transaction history for contracts';
COMMENT ON COLUMN payment_requests.platform_fee IS 'Platform commission (8% of total)';
COMMENT ON COLUMN payment_requests.student_amount IS 'Amount student receives (92% of total)';
COMMENT ON COLUMN payment_requests.payment_reference IS 'Unique reference for bank transfer verification';

-- Step 7: Create admin user (optional, for testing)
-- Password: Admin@123 (bcrypt hash)
DO $$
DECLARE
    admin_role_id BIGINT;
    admin_user_id BIGINT;
BEGIN
    -- Get ADMIN role ID
    SELECT role_id INTO admin_role_id FROM roles WHERE name = 'ADMIN';

    -- Insert admin user if not exists
    INSERT INTO users (email, password_hash, full_name, status, token_version, created_at, updated_at)
    VALUES ('admin@skillnest.com', '$2a$10$YourBcryptHashHere', 'System Admin', 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    ON CONFLICT (email) DO NOTHING
    RETURNING user_id INTO admin_user_id;

    -- Assign ADMIN role
    IF admin_user_id IS NOT NULL AND admin_role_id IS NOT NULL THEN
        INSERT INTO user_roles (user_id, role_id)
        VALUES (admin_user_id, admin_role_id)
        ON CONFLICT DO NOTHING;
    END IF;
END $$;

-- Step 8: Create manager user (optional, for testing)
-- Password: Manager@123 (bcrypt hash)
DO $$
DECLARE
    manager_role_id BIGINT;
    manager_user_id BIGINT;
BEGIN
    -- Get MANAGER role ID
    SELECT role_id INTO manager_role_id FROM roles WHERE name = 'MANAGER';

    -- Insert manager user if not exists
    INSERT INTO users (email, password_hash, full_name, status, token_version, created_at, updated_at)
    VALUES ('manager@skillnest.com', '$2a$10$YourBcryptHashHere', 'System Manager', 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    ON CONFLICT (email) DO NOTHING
    RETURNING user_id INTO manager_user_id;

    -- Assign MANAGER role
    IF manager_user_id IS NOT NULL AND manager_role_id IS NOT NULL THEN
        INSERT INTO user_roles (user_id, role_id)
        VALUES (manager_user_id, manager_role_id)
        ON CONFLICT DO NOTHING;
    END IF;
END $$;

COMMIT;

