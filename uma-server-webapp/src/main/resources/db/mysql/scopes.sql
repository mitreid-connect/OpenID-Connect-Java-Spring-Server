--
-- Turn off autocommit and start a transaction so that we can use the temp tables
--

SET AUTOCOMMIT = 0;

START TRANSACTION;

INSERT INTO system_scope (uuid, scope, description, icon, restricted, default_scope) VALUES
  ('a69e2c8a-1644-4197-ab7e-04431b4250fe', 'uma_protection', 'manage protected resources', 'briefcase', false, false),
  ('deb7ab06-4b3e-4f51-b098-72467fb8274c', 'uma_authorization', 'request access to protected resources', 'share', false, false);


COMMIT;

SET AUTOCOMMIT = 1;
