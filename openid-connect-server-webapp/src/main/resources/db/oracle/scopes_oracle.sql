--
-- Insert scope information into the temporary tables.
-- 

INSERT INTO system_scope_TEMP (scope, description, icon, restricted, default_scope, structured, structured_param_description) VALUES
  ('openid', 'log in using your identity', 'user', 0, 1, 0, null);
INSERT INTO system_scope_TEMP (scope, description, icon, restricted, default_scope, structured, structured_param_description) VALUES
  ('profile', 'basic profile information', 'list-alt', 0, 1, 0, null);
INSERT INTO system_scope_TEMP (scope, description, icon, restricted, default_scope, structured, structured_param_description) VALUES
  ('email', 'email address', 'envelope', 0, 1, 0, null);
INSERT INTO system_scope_TEMP (scope, description, icon, restricted, default_scope, structured, structured_param_description) VALUES
  ('address', 'physical address', 'home', 0, 1, 0, null);
INSERT INTO system_scope_TEMP (scope, description, icon, restricted, default_scope, structured, structured_param_description) VALUES
  ('phone', 'telephone number', 'bell', 0, 1, 0, null);
INSERT INTO system_scope_TEMP (scope, description, icon, restricted, default_scope, structured, structured_param_description) VALUES
  ('offline_access', 'offline access', 'time', 0, 0, 0, null);
  
--
-- Merge the temporary scopes safely into the database. This is a two-step process to keep scopes from being created on every startup with a persistent store.
--

MERGE INTO system_scope
	USING (SELECT scope, description, icon, restricted, default_scope, structured, structured_param_description FROM system_scope_TEMP) vals
	ON (vals.scope = system_scope.scope)
	WHEN NOT MATCHED THEN
	  INSERT (id, scope, description, icon, restricted, default_scope, structured, structured_param_description) VALUES(system_scope_seq.nextval, vals.scope,
	  vals.description, vals.icon, vals.restricted, vals.default_scope, vals.structured, vals.structured_param_description);