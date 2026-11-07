-- Optional: use after the Lab 3 upgrade script when the database already has data.
-- These credentials are for local coursework demonstrations only.
INSERT INTO users (
    username, password_hash, app_role, enabled, locked, full_name, contact_details, version
) VALUES
    ('admin', '$2a$10$cByXyAf5naMX0VROk5fN6OKKZINDSD0v3wZHpH26Pnr18DlX8owR2', 'ADMIN', TRUE, FALSE, 'Администратор бюро', 'admin@example.com', 0),
    ('sample.user', '$2a$10$egFtsMbg3CJZ0LyIJ/4ADOGkhxgYyN12BbU2WOpRLwpwVI9RHVvz.', 'USER', TRUE, FALSE, 'Анна Лебедева', 'anna@example.com', 0)
ON CONFLICT (username) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    app_role = EXCLUDED.app_role,
    enabled = EXCLUDED.enabled,
    locked = EXCLUDED.locked,
    full_name = EXCLUDED.full_name,
    contact_details = EXCLUDED.contact_details;
