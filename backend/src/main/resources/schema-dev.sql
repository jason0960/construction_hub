-- Create shedlock table for dev/test profile (Flyway is disabled for H2)
CREATE TABLE IF NOT EXISTS shedlock (
    name        VARCHAR(64) NOT NULL PRIMARY KEY,
    lock_until  TIMESTAMP NOT NULL,
    locked_at   TIMESTAMP NOT NULL,
    locked_by   VARCHAR(255) NOT NULL
);
