UPDATE metadata set preview = null;
UPDATE metadata_audit set preview = null;
ALTER TABLE metadata ALTER COLUMN preview TYPE BYTEA USING preview::bytea;
ALTER TABLE metadata_audit ALTER COLUMN preview TYPE BYTEA USING preview::bytea;