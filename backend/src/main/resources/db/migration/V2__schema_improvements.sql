-- =============================================
-- V2 - Schema improvements
-- =============================================

-- ----------------------------
-- 1. Convert TIMESTAMP to TIMESTAMPTZ for timezone safety
-- ----------------------------
ALTER TABLE organizations ALTER COLUMN created_at TYPE TIMESTAMPTZ;
ALTER TABLE organizations ALTER COLUMN updated_at TYPE TIMESTAMPTZ;

ALTER TABLE users ALTER COLUMN created_at TYPE TIMESTAMPTZ;
ALTER TABLE users ALTER COLUMN updated_at TYPE TIMESTAMPTZ;

ALTER TABLE clients ALTER COLUMN created_at TYPE TIMESTAMPTZ;
ALTER TABLE clients ALTER COLUMN updated_at TYPE TIMESTAMPTZ;
ALTER TABLE clients ALTER COLUMN deleted_at TYPE TIMESTAMPTZ;

ALTER TABLE jobs ALTER COLUMN created_at TYPE TIMESTAMPTZ;
ALTER TABLE jobs ALTER COLUMN updated_at TYPE TIMESTAMPTZ;
ALTER TABLE jobs ALTER COLUMN deleted_at TYPE TIMESTAMPTZ;

ALTER TABLE workers ALTER COLUMN created_at TYPE TIMESTAMPTZ;
ALTER TABLE workers ALTER COLUMN updated_at TYPE TIMESTAMPTZ;

ALTER TABLE crew_assignments ALTER COLUMN created_at TYPE TIMESTAMPTZ;

ALTER TABLE time_entries ALTER COLUMN clock_in TYPE TIMESTAMPTZ;
ALTER TABLE time_entries ALTER COLUMN clock_out TYPE TIMESTAMPTZ;
ALTER TABLE time_entries ALTER COLUMN created_at TYPE TIMESTAMPTZ;

ALTER TABLE permits ALTER COLUMN created_at TYPE TIMESTAMPTZ;
ALTER TABLE permits ALTER COLUMN updated_at TYPE TIMESTAMPTZ;

ALTER TABLE materials ALTER COLUMN created_at TYPE TIMESTAMPTZ;
ALTER TABLE materials ALTER COLUMN updated_at TYPE TIMESTAMPTZ;

ALTER TABLE job_notes ALTER COLUMN created_at TYPE TIMESTAMPTZ;

ALTER TABLE documents ALTER COLUMN created_at TYPE TIMESTAMPTZ;

ALTER TABLE invoices ALTER COLUMN created_at TYPE TIMESTAMPTZ;
ALTER TABLE invoices ALTER COLUMN updated_at TYPE TIMESTAMPTZ;
ALTER TABLE invoices ALTER COLUMN deleted_at TYPE TIMESTAMPTZ;

ALTER TABLE notification_log ALTER COLUMN sent_at TYPE TIMESTAMPTZ;
ALTER TABLE notification_log ALTER COLUMN read_at TYPE TIMESTAMPTZ;

ALTER TABLE shedlock ALTER COLUMN lock_until TYPE TIMESTAMPTZ;
ALTER TABLE shedlock ALTER COLUMN locked_at TYPE TIMESTAMPTZ;

ALTER TABLE refresh_tokens ALTER COLUMN expires_at TYPE TIMESTAMPTZ;
ALTER TABLE refresh_tokens ALTER COLUMN created_at TYPE TIMESTAMPTZ;

-- ----------------------------
-- 2. Unique constraint on invoice_number per organization
-- ----------------------------
CREATE UNIQUE INDEX idx_invoices_number_unique
    ON invoices (organization_id, invoice_number)
    WHERE invoice_number IS NOT NULL;

-- ----------------------------
-- 3. Partial indexes for soft-deleted records
--    (queries almost always filter deleted_at IS NULL)
-- ----------------------------
CREATE INDEX idx_jobs_active ON jobs (organization_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_clients_active ON clients (organization_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_invoices_active ON invoices (organization_id)
    WHERE deleted_at IS NULL;

-- ----------------------------
-- 4. Auto-update updated_at trigger function
-- ----------------------------
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_organizations_updated_at BEFORE UPDATE ON organizations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_clients_updated_at BEFORE UPDATE ON clients
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_jobs_updated_at BEFORE UPDATE ON jobs
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_workers_updated_at BEFORE UPDATE ON workers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_permits_updated_at BEFORE UPDATE ON permits
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_materials_updated_at BEFORE UPDATE ON materials
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_invoices_updated_at BEFORE UPDATE ON invoices
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ----------------------------
-- 5. Index for refresh token cleanup queries
-- ----------------------------
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens (expires_at);
