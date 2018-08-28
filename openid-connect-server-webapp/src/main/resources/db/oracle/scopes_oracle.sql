--
-- Insert scope information into the temporary tables.
-- 

INSERT INTO system_scope_TEMP (scope, description, icon, restricted, default_scope) VALUES
  ('openid', 'log in using your identity', 'user', 0, 1);
INSERT INTO system_scope_TEMP (scope, description, icon, restricted, default_scope) VALUES
  ('profile', 'basic profile information', 'list-alt', 0, 1);
INSERT INTO system_scope_TEMP (scope, description, icon, restricted, default_scope) VALUES
  ('email', 'email address', 'envelope', 0, 1);
INSERT INTO system_scope_TEMP (scope, description, icon, restricted, default_scope) VALUES
  ('address', 'physical address', 'home', 0, 1);
INSERT INTO system_scope_TEMP (scope, description, icon, restricted, default_scope) VALUES
  ('phone', 'telephone number', 'bell', 0, 1, 0);
INSERT INTO system_scope_TEMP (scope, description, icon, restricted, default_scope) VALUES
  ('offline_access', 'offline access', 'time', 0, 0);
--
-- Merge the temporary scopes safely into the database. This is a two-step process to keep scopes from being created on every startup with a persistent store.
--

MERGE INTO system_scope
	USING (SELECT scope, description, icon, restricted, default_scope FROM system_scope_TEMP) vals
	ON (vals.scope = system_scope.scope)
	WHEN NOT MATCHED THEN
	  INSERT (id, scope, description, icon, restricted, default_scope) VALUES(system_scope_seq.nextval, vals.scope,
	  vals.description, vals.icon, vals.restricted, vals.default_scope);
