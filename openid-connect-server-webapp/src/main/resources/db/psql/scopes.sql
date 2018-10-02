--
-- Turn off autocommit and start a transaction so that we can use the temp tables
--

SET AUTOCOMMIT = 0;

START TRANSACTION;


INSERT INTO system_scope (uuid, scope, description, icon, restricted, default_scope) VALUES
  ('16d82057-47a9-4c65-b6b0-d5d5770c4d6b', 'openid', 'log in using your identity', 'user', 0, 1),
  ('a61a2be1-09be-409c-9f89-7309a4f058e4', 'profile', 'basic profile information', 'list-alt', 0, 1),
  ('5b6a969b-9d7f-43cc-8844-3a0e6932b7f2', 'email', 'email address', 'envelope', 0, 1),
  ('f478d5a7-c57e-4083-b1c9-21e91b93500f', 'address', 'physical address', 'home', 0, 1),
  ('aca1bde1-c453-4fd4-9bc1-2b1e20251eeb', 'phone', 'telephone number', 'bell', 0, 1),
  ('2602cc29-5934-44a6-8f37-5f01d9faddb3', 'offline_access', 'offline access', 'time', 0, 0);

  
COMMIT;

SET AUTOCOMMIT = 1;

