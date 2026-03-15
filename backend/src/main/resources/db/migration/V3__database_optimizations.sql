-- =============================================
-- V3 - Database Optimizations
-- =============================================
-- Fixes:
--   1. Add hourly_rate_snapshot to time_entries (prevents retroactive rate recalculation)
--   2. Add updated_at to time_entries and crew_assignments
--   3. Add organization_id to time_entries (direct tenancy without joining through jobs)
--   4. Add deleted_at to workers (consistent soft-delete pattern)
--   5. Composite index on time_entries(job_id, worker_id)
--   6. Range index on time_entries(job_id, entry_date)
--   7. Trigger to keep materials.total in sync at DB level
--   8. Partial index on active workers

-- ----------------------------
-- 1. Hourly rate snapshot on time entries
--    NOTE: Backfill uses CURRENT worker rate since historical rates were not captured.
-- ----------------------------
ALTER TABLE time_entries ADD COLUMN hourly_rate_snapshot DECIMAL(8, 2);

-- Backfill from current worker rates (best-effort for existing data)
UPDATE time_entries te
SET hourly_rate_snapshot = w.hourly_rate
FROM workers w
WHERE te.worker_id = w.id;

-- Make NOT NULL after backfill
ALTER TABLE time_entries ALTER COLUMN hourly_rate_snapshot SET NOT NULL;
ALTER TABLE time_entries ALTER COLUMN hourly_rate_snapshot SET DEFAULT 0;

-- ----------------------------
-- 2. Add updated_at to time_entries and crew_assignments
-- ----------------------------
ALTER TABLE time_entries ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
ALTER TABLE crew_assignments ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

-- Auto-update triggers (reuse function from V2)
CREATE TRIGGER trg_time_entries_updated_at BEFORE UPDATE ON time_entries
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_crew_assignments_updated_at BEFORE UPDATE ON crew_assignments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ----------------------------
-- 3. Add organization_id to time_entries for direct tenancy
-- ----------------------------
ALTER TABLE time_entries ADD COLUMN organization_id BIGINT;

-- Backfill from jobs table
UPDATE time_entries te
SET organization_id = j.organization_id
FROM jobs j
WHERE te.job_id = j.id;

-- Make NOT NULL and add FK after backfill
ALTER TABLE time_entries ALTER COLUMN organization_id SET NOT NULL;

ALTER TABLE time_entries
    ADD CONSTRAINT fk_time_entries_organization
    FOREIGN KEY (organization_id) REFERENCES organizations(id);

CREATE INDEX idx_time_entries_organization ON time_entries(organization_id);

-- ----------------------------
-- 4. Add deleted_at to workers (consistent soft-delete)
-- ----------------------------
ALTER TABLE workers ADD COLUMN deleted_at TIMESTAMPTZ;

-- Partial index for active (non-deleted) workers
CREATE INDEX idx_workers_active ON workers (organization_id)
    WHERE deleted_at IS NULL;

-- ----------------------------
-- 5. Composite index on time_entries(job_id, worker_id)
--    Used by calculateHoursByJobAndWorker queries
-- ----------------------------
CREATE INDEX idx_time_entries_job_worker ON time_entries(job_id, worker_id);

-- ----------------------------
-- 6. Range index on time_entries(job_id, entry_date)
--    Supports date-range filtering within a job
-- ----------------------------
CREATE INDEX idx_time_entries_job_date ON time_entries(job_id, entry_date);

-- ----------------------------
-- 7. DB-level trigger to keep materials.total in sync
--    Prevents stale values from raw SQL updates outside JPA
-- ----------------------------
CREATE OR REPLACE FUNCTION calculate_material_total()
RETURNS TRIGGER AS $$
BEGIN
    NEW.total = COALESCE(NEW.quantity, 0) * COALESCE(NEW.unit_cost, 0);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_materials_calculate_total
    BEFORE INSERT OR UPDATE OF quantity, unit_cost ON materials
    FOR EACH ROW EXECUTE FUNCTION calculate_material_total();
