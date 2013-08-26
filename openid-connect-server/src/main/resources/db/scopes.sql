--
-- Turn off autocommit and start a transaction so that we can use the temp tables
--

SET AUTOCOMMIT FALSE;

START TRANSACTION;

--
-- Insert scope information into the temporary tables.
-- 

INSERT INTO system_scope_TEMP (scope, description, icon, allow_dyn_reg, default_scope, structured, structured_param_description) VALUES
  ('openid', 'log in using your identity', 'user', true, true, false, NULL),
  ('profile', 'basic profile information', 'list-alt', true, true, false, NULL),
  ('email', 'email address', 'envelope', true, true, false, NULL),
  ('address', 'physical address', 'home', true, true, false, NULL),
  ('phone', 'telephone number', 'bell', true, true, false, NULL),
  ('search', 'BB+ Document Search', 'circle-arrow-down', true, true, true, 'Which Record? (Optional.)'),
  ('summary', 'BB+ Clinical Summary', 'circle-arrow-down', true, true, true, 'Which Record? (Optional.)');
  

--
-- Merge the temporary scopes safely into the database. This is a two-step process to keep scopes from being created on every startup with a persistent store.
--

MERGE INTO system_scope
	USING (SELECT scope, description, icon, allow_dyn_reg, default_scope, structured, structured_param_description FROM system_scope_TEMP) AS vals(scope, description, icon, allow_dyn_reg, default_scope, structured, structured_param_description)
	ON vals.scope = system_scope.scope
	WHEN NOT MATCHED THEN
	  INSERT (scope, description, icon, allow_dyn_reg, default_scope, structured, structured_param_description) VALUES(vals.scope, vals.description, vals.icon, vals.allow_dyn_reg, vals.default_scope, structured, vals.structured_param_description);

COMMIT;

SET AUTOCOMMIT TRUE;
