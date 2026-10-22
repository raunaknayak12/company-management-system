-- 01_roles.sql: Database roles and schemas
-- Operational user
DO
$do$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'cms_app') THEN
      CREATE ROLE cms_app WITH LOGIN PASSWORD 'cmspassword';
   END IF;
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'dw_user') THEN
      CREATE ROLE dw_user WITH LOGIN PASSWORD 'dwpassword';
   END IF;
END
$do$;

-- Create warehouse schema
CREATE SCHEMA IF NOT EXISTS dw AUTHORIZATION dw_user;

-- Public schema permissions for operational user
GRANT ALL ON SCHEMA public TO cms_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO cms_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO cms_app;

-- DW schema permissions for dw_user
GRANT ALL ON SCHEMA dw TO dw_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA dw GRANT ALL ON TABLES TO dw_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA dw GRANT ALL ON SEQUENCES TO dw_user;

-- dw_user can read public schema for ETL extraction
GRANT USAGE ON SCHEMA public TO dw_user;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO dw_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO dw_user;
