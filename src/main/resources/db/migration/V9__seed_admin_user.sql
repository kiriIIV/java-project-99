UPDATE users
SET password_hash = '$2a$10$SVaf42tXKKeeGwgaFuoRzur4a223pMCjphMGixzwdlOl8JYCTWlZm',
    first_name    = 'Admin',
    last_name     = 'User'
WHERE email = 'admin@local';

INSERT INTO users (email, password_hash, first_name, last_name, created_at)
SELECT 'admin@local',
       '$2a$10$SVaf42tXKKeeGwgaFuoRzur4a223pMCjphMGixzwdlOl8JYCTWlZm',
       'Admin',
       'User',
       CURRENT_DATE
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@local');
