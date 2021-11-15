--
-- Turn off autocommit and start a transaction so that we can use the temp tables
--

SET AUTOCOMMIT = 0;

START TRANSACTION;

CREATE TEMPORARY TABLE IF NOT EXISTS system_scope_TEMP (
    scope VARCHAR(256),
    description VARCHAR(4096),
    icon VARCHAR(256),
    restricted BOOLEAN,
    default_scope BOOLEAN
);
--
-- Insert scope information into the temporary tables.
--

INSERT INTO system_scope_TEMP (scope, description, icon, restricted, default_scope) VALUES
('openid', 'log in using your identity', 'user', false, true),
('profile', 'basic profile information', 'list-alt', false, true),
('email', 'email address', 'envelope', false, true),
('address', 'physical address', 'home', false, true),
('phone', 'telephone number', 'bell', false, true),
('offline_access', 'offline access', 'time', false, false),
('perun_api', 'calls to Perun API in your roles', 'cog', true, false);

--
-- Merge the temporary scopes safely into the database. This is a two-step process to keep scopes from being created on every startup with a persistent store.
--

INSERT INTO system_scope (scope, description, icon, restricted, default_scope)
SELECT scope, description, icon, restricted, default_scope FROM system_scope_TEMP
ON DUPLICATE KEY UPDATE system_scope.scope = system_scope.scope;

COMMIT;

SET AUTOCOMMIT = 1;

