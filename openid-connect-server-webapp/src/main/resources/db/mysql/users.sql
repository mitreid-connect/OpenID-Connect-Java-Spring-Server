--
-- Turn off autocommit and start a transaction so that we can use the temp tables
--

SET AUTOCOMMIT = 0;

START TRANSACTION;


INSERT INTO user (uuid, host_uuid, username, password, enabled, account_non_expired, account_non_locked, credentials_non_expired) VALUES
  ('3e75c5f0-c26f-4f48-b871-d4b7ec3c03c1', '0629d968-4eb4-467d-b45f-f4b1a1d3e7f0', 'admin','$2a$11$ggXxcPPUfcC1vNVtoVPENuxkknUogsg.qdVMNnUxzH74X/DJBPmOC', 1, 1, 1, 1),
  ('85b9306b-5c3e-4297-b35b-b84dcbd158a7', '0629d968-4eb4-467d-b45f-f4b1a1d3e7f0', 'user','$2a$11$ggXxcPPUfcC1vNVtoVPENuxkknUogsg.qdVMNnUxzH74X/DJBPmOC', 1, 1, 1, 1);


INSERT INTO user_authority (user_uuid, authority) VALUES
  ('3e75c5f0-c26f-4f48-b871-d4b7ec3c03c1', 'ROLE_ADMIN'),
  ('3e75c5f0-c26f-4f48-b871-d4b7ec3c03c1', 'ROLE_USER'),
  ('85b9306b-5c3e-4297-b35b-b84dcbd158a7', 'ROLE_USER');
    
-- By default, the username column here has to match the username column in the users table, above
INSERT INTO user_info (user_uuid, host_uuid, sub, name, email, email_verified) VALUES
  ('3e75c5f0-c26f-4f48-b871-d4b7ec3c03c1', '0629d968-4eb4-467d-b45f-f4b1a1d3e7f0', '90342.ASDFJWFA', 'Demo Admin','admin@example.com', 1),
  ('85b9306b-5c3e-4297-b35b-b84dcbd158a7', '0629d968-4eb4-467d-b45f-f4b1a1d3e7f0', '01921.FLANRJQW', 'Demo User','user@example.com', 1);


COMMIT;

SET AUTOCOMMIT = 1;
