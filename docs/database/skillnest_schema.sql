CREATE SCHEMA skillnest AUTHORIZATION admin;
SET search_path TO skillnest;
BEGIN;

-- ========= 0) ENUM TYPES =========
DO $$ BEGIN
    CREATE TYPE user_status AS ENUM ('ACTIVE', 'SUSPENDED', 'DELETED');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE project_status AS ENUM ('DRAFT', 'OPEN', 'IN_PROGRESS', 'CLOSED', 'CANCELLED');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE proposal_status AS ENUM ('SUBMITTED', 'SHORTLISTED', 'ACCEPTED', 'REJECTED', 'WITHDRAWN');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE contract_status AS ENUM ('PENDING', 'ACTIVE', 'COMPLETED', 'CANCELLED');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE dispute_status AS ENUM ('OPEN', 'IN_REVIEW', 'RESOLVED', 'REJECTED', 'CLOSED');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE project_type AS ENUM ('FIXED_PRICE', 'HOURLY');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE message_type AS ENUM ('TEXT', 'FILE', 'SYSTEM');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE notification_type AS ENUM ('SYSTEM', 'PROJECT', 'PROPOSAL', 'CONTRACT', 'PAYMENT', 'DISPUTE', 'CHAT');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE milestone_status AS ENUM ('PENDING', 'IN_PROGRESS', 'SUBMITTED', 'APPROVED', 'REJECTED', 'PAID');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE submission_status AS ENUM ('SUBMITTED', 'REVISION_REQUESTED', 'APPROVED', 'REJECTED');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE payment_status AS ENUM ('PENDING', 'AUTHORIZED', 'PAID', 'FAILED', 'REFUNDED', 'CANCELLED');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;


-- ========= 1) USERS / ROLES / PROFILES =========
CREATE TABLE IF NOT EXISTS users (
                                     user_id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                     email             VARCHAR(255) NOT NULL,
                                     password_hash     VARCHAR(255) NOT NULL,
                                     full_name         VARCHAR(200) NOT NULL,
                                     avatar_url        TEXT,
                                     status            user_status NOT NULL DEFAULT 'ACTIVE',
                                     phone             VARCHAR(30),
                                     token_version     INT NOT NULL DEFAULT 0,
                                     created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                     updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email ON users (lower(email));

CREATE TABLE IF NOT EXISTS roles (
                                     role_id           SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                     name              VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS user_roles (
                                          user_id           BIGINT NOT NULL,
                                          role_id           SMALLINT NOT NULL,
                                          assigned_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
                                          PRIMARY KEY (user_id, role_id),
                                          CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                                          CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS student_profiles (
                                                user_id           BIGINT PRIMARY KEY,
                                                university        VARCHAR(255),
                                                major             VARCHAR(255),
                                                bio               TEXT,
                                                year_of_study     SMALLINT,
                                                cv_url            TEXT,
                                                created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                                updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                                CONSTRAINT fk_student_profiles_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS client_profiles (
                                               user_id           BIGINT PRIMARY KEY,
                                               company_name      VARCHAR(255),
                                               tax_code          VARCHAR(100),
                                               bio               TEXT,
                                               created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                               updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                               CONSTRAINT fk_client_profiles_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);


-- ========= 2) SKILLS / PORTFOLIO =========
CREATE TABLE IF NOT EXISTS skills (
                                      skill_id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                      name              VARCHAR(120) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS user_skills (
                                           user_id           BIGINT NOT NULL,
                                           skill_id          BIGINT NOT NULL,
                                           level             SMALLINT NOT NULL DEFAULT 1 CHECK (level BETWEEN 1 AND 5),
                                           PRIMARY KEY (user_id, skill_id),
                                           CONSTRAINT fk_user_skills_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                                           CONSTRAINT fk_user_skills_skill FOREIGN KEY (skill_id) REFERENCES skills(skill_id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS ix_user_skills_skill ON user_skills (skill_id);

CREATE TABLE IF NOT EXISTS portfolio_items (
                                               portfolio_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                               user_id           BIGINT NOT NULL,
                                               title             VARCHAR(200) NOT NULL,
                                               description       TEXT,
                                               link              TEXT,
                                               media_url         TEXT,
                                               created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                               CONSTRAINT fk_portfolio_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_portfolio_user ON portfolio_items (user_id);


-- ========= 3) CATEGORY / PROJECT =========
CREATE TABLE IF NOT EXISTS categories (
                                          category_id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                          name              VARCHAR(120) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS projects (
                                        project_id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                        client_id         BIGINT NOT NULL,
                                        title             VARCHAR(250) NOT NULL,
                                        description       TEXT NOT NULL,
                                        project_type      project_type NOT NULL DEFAULT 'FIXED_PRICE',
                                        budget_min        NUMERIC(12,2),
                                        budget_max        NUMERIC(12,2),
                                        currency          VARCHAR(10) NOT NULL DEFAULT 'VND',
                                        status            project_status NOT NULL DEFAULT 'OPEN',
                                        created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                        updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                        CONSTRAINT fk_projects_client FOREIGN KEY (client_id) REFERENCES users(user_id) ON DELETE RESTRICT,
                                        CONSTRAINT ck_project_budget CHECK (
                                            (budget_min IS NULL AND budget_max IS NULL) OR
                                            (budget_min IS NOT NULL AND budget_max IS NOT NULL AND budget_min <= budget_max)
                                            )
);

CREATE INDEX IF NOT EXISTS ix_projects_client ON projects (client_id);
CREATE INDEX IF NOT EXISTS ix_projects_status_created ON projects (status, created_at DESC);

CREATE TABLE IF NOT EXISTS project_categories (
                                                  project_id        BIGINT NOT NULL,
                                                  category_id       BIGINT NOT NULL,
                                                  PRIMARY KEY (project_id, category_id),
                                                  CONSTRAINT fk_proj_cat_project FOREIGN KEY (project_id) REFERENCES projects(project_id) ON DELETE CASCADE,
                                                  CONSTRAINT fk_proj_cat_category FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS ix_project_categories_category ON project_categories (category_id);

CREATE TABLE IF NOT EXISTS project_skills (
                                              project_id        BIGINT NOT NULL,
                                              skill_id          BIGINT NOT NULL,
                                              priority          SMALLINT NOT NULL DEFAULT 3 CHECK (priority BETWEEN 1 AND 5),
                                              PRIMARY KEY (project_id, skill_id),
                                              CONSTRAINT fk_project_skills_project FOREIGN KEY (project_id) REFERENCES projects(project_id) ON DELETE CASCADE,
                                              CONSTRAINT fk_project_skills_skill FOREIGN KEY (skill_id) REFERENCES skills(skill_id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS ix_project_skills_skill ON project_skills (skill_id);


-- ========= 4) PROPOSAL / CONTRACT =========
CREATE TABLE IF NOT EXISTS proposals (
                                         proposal_id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                         project_id        BIGINT NOT NULL,
                                         student_id        BIGINT NOT NULL,
                                         cover_letter      TEXT,
                                         proposed_price    NUMERIC(12,2),
                                         currency          VARCHAR(10) NOT NULL DEFAULT 'VND',
                                         duration_days     INT,
                                         status            proposal_status NOT NULL DEFAULT 'SUBMITTED',
                                         created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                         updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                         CONSTRAINT fk_proposals_project FOREIGN KEY (project_id) REFERENCES projects(project_id) ON DELETE CASCADE,
                                         CONSTRAINT fk_proposals_student FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE RESTRICT
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_proposals_project_student ON proposals (project_id, student_id);
CREATE INDEX IF NOT EXISTS ix_proposals_project_status ON proposals (project_id, status, created_at DESC);
CREATE INDEX IF NOT EXISTS ix_proposals_student_created ON proposals (student_id, created_at DESC);

CREATE TABLE IF NOT EXISTS contracts (
                                         contract_id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                         project_id        BIGINT NOT NULL,
                                         proposal_id       BIGINT NOT NULL UNIQUE,
                                         client_id         BIGINT NOT NULL,
                                         student_id        BIGINT NOT NULL,
                                         agreed_price      NUMERIC(12,2) NOT NULL,
                                         currency          VARCHAR(10) NOT NULL DEFAULT 'VND',
                                         start_at          TIMESTAMPTZ,
                                         end_at            TIMESTAMPTZ,
                                         status            contract_status NOT NULL DEFAULT 'PENDING',
                                         created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                         updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                         CONSTRAINT fk_contracts_project FOREIGN KEY (project_id) REFERENCES projects(project_id) ON DELETE RESTRICT,
                                         CONSTRAINT fk_contracts_proposal FOREIGN KEY (proposal_id) REFERENCES proposals(proposal_id) ON DELETE RESTRICT,
                                         CONSTRAINT fk_contracts_client FOREIGN KEY (client_id) REFERENCES users(user_id) ON DELETE RESTRICT,
                                         CONSTRAINT fk_contracts_student FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE RESTRICT,
                                         CONSTRAINT ck_contract_dates CHECK (end_at IS NULL OR start_at IS NULL OR start_at <= end_at)
);

CREATE INDEX IF NOT EXISTS ix_contracts_client_status ON contracts (client_id, status, created_at DESC);
CREATE INDEX IF NOT EXISTS ix_contracts_student_status ON contracts (student_id, status, created_at DESC);
CREATE INDEX IF NOT EXISTS ix_contracts_project ON contracts (project_id);


-- ========= 5) CONVERSATION / MESSAGE =========
CREATE TABLE IF NOT EXISTS conversations (
                                             conversation_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                             contract_id       BIGINT NOT NULL UNIQUE,
                                             created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                             CONSTRAINT fk_conversations_contract FOREIGN KEY (contract_id) REFERENCES contracts(contract_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS messages (
                                        message_id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                        conversation_id   BIGINT NOT NULL,
                                        sender_id         BIGINT NOT NULL,
                                        type              message_type NOT NULL DEFAULT 'TEXT',
                                        content           TEXT,
                                        file_url          TEXT,
                                        sent_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
                                        CONSTRAINT fk_messages_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(conversation_id) ON DELETE CASCADE,
                                        CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE RESTRICT,
                                        CONSTRAINT ck_message_payload CHECK (
                                            (type = 'TEXT' AND content IS NOT NULL) OR
                                            (type = 'FILE' AND file_url IS NOT NULL) OR
                                            (type = 'SYSTEM')
                                            )
);

CREATE INDEX IF NOT EXISTS ix_messages_conversation_time ON messages (conversation_id, sent_at DESC);
CREATE INDEX IF NOT EXISTS ix_messages_sender_time ON messages (sender_id, sent_at DESC);


-- ========= 6) DISPUTE =========
CREATE TABLE IF NOT EXISTS disputes (
                                        dispute_id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                        contract_id       BIGINT NOT NULL,
                                        opened_by         BIGINT NOT NULL,
                                        reason            TEXT NOT NULL,
                                        status            dispute_status NOT NULL DEFAULT 'OPEN',
                                        created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                        resolved_at       TIMESTAMPTZ,
                                        CONSTRAINT fk_disputes_contract FOREIGN KEY (contract_id) REFERENCES contracts(contract_id) ON DELETE CASCADE,
                                        CONSTRAINT fk_disputes_opened_by FOREIGN KEY (opened_by) REFERENCES users(user_id) ON DELETE RESTRICT,
                                        CONSTRAINT ck_dispute_resolve_time CHECK (resolved_at IS NULL OR created_at <= resolved_at)
);

CREATE INDEX IF NOT EXISTS ix_disputes_contract_status ON disputes (contract_id, status, created_at DESC);
CREATE INDEX IF NOT EXISTS ix_disputes_opened_by ON disputes (opened_by, created_at DESC);


-- ========= 7) MILESTONE / SUBMISSION =========
CREATE TABLE IF NOT EXISTS milestones (
                                          milestone_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                          contract_id       BIGINT NOT NULL,
                                          title             VARCHAR(200) NOT NULL,
                                          amount            NUMERIC(12,2) NOT NULL,
                                          due_date          DATE,
                                          status            milestone_status NOT NULL DEFAULT 'PENDING',
                                          created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                          CONSTRAINT fk_milestones_contract FOREIGN KEY (contract_id) REFERENCES contracts(contract_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_milestones_contract_status ON milestones (contract_id, status);

CREATE TABLE IF NOT EXISTS submissions (
                                           submission_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                           milestone_id      BIGINT NOT NULL,
                                           submitted_by      BIGINT NOT NULL,
                                           content           TEXT,
                                           file_url          TEXT,
                                           status            submission_status NOT NULL DEFAULT 'SUBMITTED',
                                           created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                           CONSTRAINT fk_submissions_milestone FOREIGN KEY (milestone_id) REFERENCES milestones(milestone_id) ON DELETE CASCADE,
                                           CONSTRAINT fk_submissions_user FOREIGN KEY (submitted_by) REFERENCES users(user_id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS ix_submissions_milestone_time ON submissions (milestone_id, created_at DESC);


-- ========= 8) REVIEW / PAYMENT / NOTIFICATION / AI =========
CREATE TABLE IF NOT EXISTS reviews (
                                       review_id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                       contract_id       BIGINT NOT NULL,
                                       reviewer_id       BIGINT NOT NULL,
                                       reviewee_id       BIGINT NOT NULL,
                                       rating            SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
                                       comment           TEXT,
                                       created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                       CONSTRAINT fk_reviews_contract FOREIGN KEY (contract_id) REFERENCES contracts(contract_id) ON DELETE CASCADE,
                                       CONSTRAINT fk_reviews_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(user_id) ON DELETE RESTRICT,
                                       CONSTRAINT fk_reviews_reviewee FOREIGN KEY (reviewee_id) REFERENCES users(user_id) ON DELETE RESTRICT
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_reviews_contract_reviewer ON reviews (contract_id, reviewer_id);

CREATE TABLE IF NOT EXISTS payments (
                                        payment_id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                        contract_id       BIGINT NOT NULL,
                                        milestone_id      BIGINT,
                                        payer_id          BIGINT NOT NULL,
                                        payee_id          BIGINT NOT NULL,
                                        amount            NUMERIC(12,2) NOT NULL,
                                        currency          VARCHAR(10) NOT NULL DEFAULT 'VND',
                                        status            payment_status NOT NULL DEFAULT 'PENDING',
                                        provider_ref      VARCHAR(255),
                                        paid_at           TIMESTAMPTZ,
                                        created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                        CONSTRAINT fk_payments_contract FOREIGN KEY (contract_id) REFERENCES contracts(contract_id) ON DELETE CASCADE,
                                        CONSTRAINT fk_payments_milestone FOREIGN KEY (milestone_id) REFERENCES milestones(milestone_id) ON DELETE SET NULL,
                                        CONSTRAINT fk_payments_payer FOREIGN KEY (payer_id) REFERENCES users(user_id) ON DELETE RESTRICT,
                                        CONSTRAINT fk_payments_payee FOREIGN KEY (payee_id) REFERENCES users(user_id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS ix_payments_contract_status ON payments (contract_id, status, created_at DESC);
CREATE INDEX IF NOT EXISTS ix_payments_payee_time ON payments (payee_id, created_at DESC);

CREATE TABLE IF NOT EXISTS notifications (
                                             notification_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                             user_id           BIGINT NOT NULL,
                                             type              notification_type NOT NULL,
                                             payload           JSONB NOT NULL DEFAULT '{}'::jsonb,
                                             is_read           BOOLEAN NOT NULL DEFAULT false,
                                             created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                             CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_notifications_user_unread ON notifications (user_id, is_read, created_at DESC);

CREATE TABLE IF NOT EXISTS ai_interactions (
                                               ai_id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                               user_id           BIGINT NOT NULL,
                                               context_type      VARCHAR(30) NOT NULL,
                                               context_id        BIGINT,
                                               prompt            TEXT NOT NULL,
                                               response          TEXT,
                                               model             VARCHAR(100),
                                               created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                               CONSTRAINT fk_ai_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_ai_user_time ON ai_interactions (user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS ix_ai_context ON ai_interactions (context_type, context_id);

COMMIT;
