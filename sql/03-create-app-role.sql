-- Run as PostgreSQL administrator while connected to database `postgres`.
-- Replace the example password before executing. Run CREATE ROLE only once.
CREATE ROLE lost_found_app LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT
    PASSWORD 'REPLACE_WITH_A_STRONG_LOCAL_PASSWORD';
GRANT CONNECT ON DATABASE lost_and_found TO lost_found_app;
