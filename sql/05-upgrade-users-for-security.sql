-- Run once on a database created by Lab 3 before launching Lab 4.
-- Existing Lab 3 users are disabled until they register a new account.
ALTER TABLE users ADD COLUMN IF NOT EXISTS username VARCHAR(32);
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_hash VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS app_role VARCHAR(20) DEFAULT 'USER';
ALTER TABLE users ADD COLUMN IF NOT EXISTS enabled BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS locked BOOLEAN DEFAULT TRUE;

UPDATE users
SET username = 'legacy_user_' || id
WHERE username IS NULL;

UPDATE users
SET password_hash = '!disabled-legacy-account'
WHERE password_hash IS NULL;

UPDATE users
SET app_role = 'USER', enabled = FALSE, locked = TRUE
WHERE password_hash = '!disabled-legacy-account';

ALTER TABLE users ALTER COLUMN username SET NOT NULL;
ALTER TABLE users ALTER COLUMN password_hash SET NOT NULL;
ALTER TABLE users ALTER COLUMN app_role SET NOT NULL;
ALTER TABLE users ALTER COLUMN enabled SET NOT NULL;
ALTER TABLE users ALTER COLUMN locked SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_username ON users (username);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'ck_users_app_role' AND conrelid = 'users'::regclass
    ) THEN
        ALTER TABLE users ADD CONSTRAINT ck_users_app_role
            CHECK (app_role IN ('USER', 'ADMIN'));
    END IF;
END $$;
