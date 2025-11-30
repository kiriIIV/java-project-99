ALTER TABLE users
    ALTER COLUMN created_at
    TYPE DATE
    USING created_at::date;
