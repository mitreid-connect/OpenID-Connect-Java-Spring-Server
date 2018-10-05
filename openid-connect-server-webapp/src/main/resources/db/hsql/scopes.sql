--
-- Turn off autocommit and start a transaction so that we can use the temp tables
--

SET AUTOCOMMIT FALSE;

START TRANSACTION;


INSERT INTO system_scope (uuid, host_uuid, scope, description, icon, restricted, default_scope) VALUES
  ('16d82057-47a9-4c65-b6b0-d5d5770c4d6b', '6e4bf681-2ac2-4341-9fff-b32802f1a72c', 'openid', 'log in using your identity', 'user', false, true),
  ('a61a2be1-09be-409c-9f89-7309a4f058e4', '6e4bf681-2ac2-4341-9fff-b32802f1a72c', 'profile', 'basic profile information', 'list-alt', false, true),
  ('5b6a969b-9d7f-43cc-8844-3a0e6932b7f2', '6e4bf681-2ac2-4341-9fff-b32802f1a72c', 'email', 'email address', 'envelope', false, true),
  ('f478d5a7-c57e-4083-b1c9-21e91b93500f', '6e4bf681-2ac2-4341-9fff-b32802f1a72c', 'address', 'physical address', 'home', false, true),
  ('aca1bde1-c453-4fd4-9bc1-2b1e20251eeb', '6e4bf681-2ac2-4341-9fff-b32802f1a72c', 'phone', 'telephone number', 'bell', false, true),
  ('2602cc29-5934-44a6-8f37-5f01d9faddb3', '6e4bf681-2ac2-4341-9fff-b32802f1a72c', 'offline_access', 'offline access', 'time', false, false);


COMMIT;

SET AUTOCOMMIT TRUE;