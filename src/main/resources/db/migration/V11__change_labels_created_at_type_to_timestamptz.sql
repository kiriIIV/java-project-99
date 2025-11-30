ALTER TABLE labels
    ALTER COLUMN created_at
    TYPE TIMESTAMPTZ
    USING created_at::timestamptz;
