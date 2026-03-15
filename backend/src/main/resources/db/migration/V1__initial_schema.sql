-- =============================================
-- ConstructionHub - Initial Schema
-- =============================================

-- ----------------------------
-- Organizations (multi-tenancy)
-- ----------------------------
CREATE TABLE organizations (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ----------------------------
-- Users
-- ----------------------------
CREATE TABLE users (
    id                  BIGSERIAL PRIMARY KEY,
    organization_id     BIGINT NOT NULL REFERENCES organizations(id),
    email               VARCHAR(255) NOT NULL UNIQUE,
    password_hash       VARCHAR(255) NOT NULL,
    role                VARCHAR(50) NOT NULL CHECK (role IN ('OWNER', 'ADMIN', 'WORKER')),
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    phone               VARCHAR(30),
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_organization ON users(organization_id);
CREATE INDEX idx_users_email ON users(email);

-- ----------------------------
-- Clients
-- ----------------------------
CREATE TABLE clients (
    id                  BIGSERIAL PRIMARY KEY,
    organization_id     BIGINT NOT NULL REFERENCES organizations(id),
    name                VARCHAR(255) NOT NULL,
    phone               VARCHAR(30),
    email               VARCHAR(255),
    created_by          BIGINT NOT NULL REFERENCES users(id),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMP
);

CREATE INDEX idx_clients_organization ON clients(organization_id);

-- ----------------------------
-- Jobs
-- ----------------------------
CREATE TABLE jobs (
    id                  BIGSERIAL PRIMARY KEY,
    organization_id     BIGINT NOT NULL REFERENCES organizations(id),
    title               VARCHAR(255) NOT NULL,
    description         TEXT,
    client_id           BIGINT REFERENCES clients(id),
    status              VARCHAR(50) NOT NULL DEFAULT 'LEAD'
                        CHECK (status IN ('LEAD', 'ESTIMATED', 'CONTRACTED', 'IN_PROGRESS', 'ON_HOLD', 'COMPLETED', 'CANCELLED')),
    site_address        VARCHAR(255),
    site_city           VARCHAR(100),
    site_state          VARCHAR(50),
    site_zip            VARCHAR(20),
    site_unit           VARCHAR(50),
    contract_price      DECIMAL(12, 2),
    start_date          DATE,
    estimated_end_date  DATE,
    actual_end_date     DATE,
    created_by          BIGINT NOT NULL REFERENCES users(id),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMP
);

CREATE INDEX idx_jobs_organization ON jobs(organization_id);
CREATE INDEX idx_jobs_status ON jobs(organization_id, status);
CREATE INDEX idx_jobs_client ON jobs(client_id);

-- ----------------------------
-- Workers
-- ----------------------------
CREATE TABLE workers (
    id                  BIGSERIAL PRIMARY KEY,
    organization_id     BIGINT NOT NULL REFERENCES organizations(id),
    user_id             BIGINT REFERENCES users(id),
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    phone               VARCHAR(30),
    email               VARCHAR(255),
    trade               VARCHAR(100),
    hourly_rate         DECIMAL(8, 2) NOT NULL DEFAULT 0,
    status              VARCHAR(50) NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_workers_organization ON workers(organization_id);
CREATE INDEX idx_workers_user ON workers(user_id);

-- ----------------------------
-- Crew Assignments
-- ----------------------------
CREATE TABLE crew_assignments (
    id                  BIGSERIAL PRIMARY KEY,
    job_id              BIGINT NOT NULL REFERENCES jobs(id),
    worker_id           BIGINT NOT NULL REFERENCES workers(id),
    role_on_job         VARCHAR(100),
    start_date          DATE,
    end_date            DATE,
    status              VARCHAR(50) NOT NULL DEFAULT 'ASSIGNED'
                        CHECK (status IN ('ASSIGNED', 'ACTIVE', 'COMPLETED', 'REMOVED')),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),

    UNIQUE (job_id, worker_id)
);

CREATE INDEX idx_crew_assignments_job ON crew_assignments(job_id);
CREATE INDEX idx_crew_assignments_worker ON crew_assignments(worker_id);

-- ----------------------------
-- Time Entries
-- ----------------------------
CREATE TABLE time_entries (
    id                  BIGSERIAL PRIMARY KEY,
    job_id              BIGINT NOT NULL REFERENCES jobs(id),
    worker_id           BIGINT NOT NULL REFERENCES workers(id),
    entry_date          DATE NOT NULL,
    hours               DECIMAL(5, 2) NOT NULL,
    clock_in            TIMESTAMP,
    clock_out           TIMESTAMP,
    entered_by          BIGINT NOT NULL REFERENCES users(id),
    notes               TEXT,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_time_entries_job ON time_entries(job_id);
CREATE INDEX idx_time_entries_worker ON time_entries(worker_id, entry_date);

-- ----------------------------
-- Permits
-- ----------------------------
CREATE TABLE permits (
    id                      BIGSERIAL PRIMARY KEY,
    job_id                  BIGINT NOT NULL REFERENCES jobs(id),
    permit_type             VARCHAR(100) NOT NULL,
    permit_number           VARCHAR(100),
    issuing_authority       VARCHAR(255),
    status                  VARCHAR(50) NOT NULL DEFAULT 'PENDING'
                            CHECK (status IN ('PENDING', 'ACTIVE', 'EXPIRED', 'RENEWED')),
    fee                     DECIMAL(12, 2) NOT NULL DEFAULT 0,
    application_date        DATE,
    issue_date              DATE,
    expiration_date         DATE,
    reminder_days_before    INTEGER NOT NULL DEFAULT 30,
    notes                   TEXT,
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_permits_job ON permits(job_id);
CREATE INDEX idx_permits_expiration ON permits(expiration_date);
CREATE INDEX idx_permits_status ON permits(job_id, status);

-- ----------------------------
-- Materials
-- ----------------------------
CREATE TABLE materials (
    id                      BIGSERIAL PRIMARY KEY,
    job_id                  BIGINT NOT NULL REFERENCES jobs(id),
    name                    VARCHAR(255) NOT NULL,
    quantity                DECIMAL(10, 2) NOT NULL DEFAULT 1,
    unit_cost               DECIMAL(10, 2) NOT NULL DEFAULT 0,
    total                   DECIMAL(12, 2) NOT NULL DEFAULT 0,
    receipt_document_id     BIGINT,
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_materials_job ON materials(job_id);

-- ----------------------------
-- Job Notes
-- ----------------------------
CREATE TABLE job_notes (
    id                  BIGSERIAL PRIMARY KEY,
    job_id              BIGINT NOT NULL REFERENCES jobs(id),
    author_id           BIGINT NOT NULL REFERENCES users(id),
    content             TEXT NOT NULL,
    visibility          VARCHAR(20) NOT NULL DEFAULT 'SHARED'
                        CHECK (visibility IN ('SHARED', 'OWNER_ONLY')),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_job_notes_job ON job_notes(job_id);

-- ----------------------------
-- Documents
-- ----------------------------
CREATE TABLE documents (
    id                  BIGSERIAL PRIMARY KEY,
    organization_id     BIGINT NOT NULL REFERENCES organizations(id),
    job_id              BIGINT REFERENCES jobs(id),
    permit_id           BIGINT REFERENCES permits(id),
    uploaded_by         BIGINT NOT NULL REFERENCES users(id),
    file_name           VARCHAR(255) NOT NULL,
    file_type           VARCHAR(100),
    file_size_bytes     BIGINT,
    storage_key         VARCHAR(500) NOT NULL,
    category            VARCHAR(50) NOT NULL DEFAULT 'OTHER'
                        CHECK (category IN ('PHOTO', 'PERMIT', 'CONTRACT', 'PLAN', 'RECEIPT', 'OTHER')),
    description         TEXT,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_documents_job ON documents(job_id);
CREATE INDEX idx_documents_permit ON documents(permit_id);
CREATE INDEX idx_documents_organization ON documents(organization_id);

-- Add FK from materials to documents (after documents table exists)
ALTER TABLE materials
    ADD CONSTRAINT fk_materials_receipt_document
    FOREIGN KEY (receipt_document_id) REFERENCES documents(id);

-- ----------------------------
-- Invoices
-- ----------------------------
CREATE TABLE invoices (
    id                  BIGSERIAL PRIMARY KEY,
    organization_id     BIGINT NOT NULL REFERENCES organizations(id),
    job_id              BIGINT NOT NULL REFERENCES jobs(id),
    client_id           BIGINT REFERENCES clients(id),
    invoice_number      VARCHAR(50),
    type                VARCHAR(20) NOT NULL CHECK (type IN ('ESTIMATE', 'INVOICE')),
    status              VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
                        CHECK (status IN ('DRAFT', 'SENT', 'PAID', 'OVERDUE', 'CANCELLED')),
    issue_date          DATE,
    due_date            DATE,
    subtotal            DECIMAL(12, 2) NOT NULL DEFAULT 0,
    tax_rate            DECIMAL(5, 4) NOT NULL DEFAULT 0,
    tax_amount          DECIMAL(12, 2) NOT NULL DEFAULT 0,
    total               DECIMAL(12, 2) NOT NULL DEFAULT 0,
    notes               TEXT,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMP
);

CREATE INDEX idx_invoices_job ON invoices(job_id);
CREATE INDEX idx_invoices_organization ON invoices(organization_id);

-- ----------------------------
-- Invoice Line Items
-- ----------------------------
CREATE TABLE invoice_line_items (
    id                  BIGSERIAL PRIMARY KEY,
    invoice_id          BIGINT NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    description         VARCHAR(500) NOT NULL,
    quantity            DECIMAL(10, 2) NOT NULL DEFAULT 1,
    unit_price          DECIMAL(10, 2) NOT NULL DEFAULT 0,
    total               DECIMAL(12, 2) NOT NULL DEFAULT 0,
    sort_order          INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_invoice_line_items_invoice ON invoice_line_items(invoice_id);

-- ----------------------------
-- Notification Log
-- ----------------------------
CREATE TABLE notification_log (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES users(id),
    type                VARCHAR(50) NOT NULL,
    reference_id        BIGINT,
    reference_type      VARCHAR(50),
    channel             VARCHAR(20) NOT NULL CHECK (channel IN ('EMAIL', 'PUSH', 'IN_APP')),
    message             TEXT NOT NULL,
    sent_at             TIMESTAMP NOT NULL DEFAULT NOW(),
    read_at             TIMESTAMP
);

CREATE INDEX idx_notification_log_user ON notification_log(user_id, read_at);

-- ----------------------------
-- ShedLock (distributed lock table)
-- ----------------------------
CREATE TABLE shedlock (
    name        VARCHAR(64) NOT NULL PRIMARY KEY,
    lock_until  TIMESTAMP NOT NULL,
    locked_at   TIMESTAMP NOT NULL,
    locked_by   VARCHAR(255) NOT NULL
);

-- ----------------------------
-- Refresh Tokens
-- ----------------------------
CREATE TABLE refresh_tokens (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES users(id),
    token               VARCHAR(500) NOT NULL UNIQUE,
    expires_at          TIMESTAMP NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
