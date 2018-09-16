--
-- Turn off autocommit and start a transaction so that we can use the temp tables
--

SET AUTOCOMMIT FALSE;

START TRANSACTION;


INSERT INTO users (uuid, host_uuid, username, password, enabled) VALUES
  ('3e75c5f0-c26f-4f48-b871-d4b7ec3c03c1', '0629d968-4eb4-467d-b45f-f4b1a1d3e7f0', 'admin','password',true),
  ('85b9306b-5c3e-4297-b35b-b84dcbd158a7', '0629d968-4eb4-467d-b45f-f4b1a1d3e7f0', 'user','password',true);


INSERT INTO authorities (uuid, host_uuid, user_uuid, username, authority) VALUES
  ('55ada8e5-0562-4dee-9fda-9ca6fffaf831', '0629d968-4eb4-467d-b45f-f4b1a1d3e7f0', '3e75c5f0-c26f-4f48-b871-d4b7ec3c03c1', 'ROLE_ADMIN'),
  ('cbf4ee35-3a2c-413f-913b-04b06b9a5826', '0629d968-4eb4-467d-b45f-f4b1a1d3e7f0', '3e75c5f0-c26f-4f48-b871-d4b7ec3c03c1', 'ROLE_USER'),
  ('15796fcd-91dc-4c73-be0d-0b7af608c0ab', '0629d968-4eb4-467d-b45f-f4b1a1d3e7f0', '85b9306b-5c3e-4297-b35b-b84dcbd158a7', 'ROLE_USER');
    
-- By default, the username column here has to match the username column in the users table, above
INSERT INTO user_info (user_uuid, host_uuid, sub, name, email, email_verified) VALUES
  ('3e75c5f0-c26f-4f48-b871-d4b7ec3c03c1', '0629d968-4eb4-467d-b45f-f4b1a1d3e7f0', '90342.ASDFJWFA', 'Demo Admin','admin@example.com', true),
  ('85b9306b-5c3e-4297-b35b-b84dcbd158a7', '0629d968-4eb4-467d-b45f-f4b1a1d3e7f0', '01921.FLANRJQW', 'Demo User','user@example.com', true);

    
-- 
-- Close the transaction and turn autocommit back on
-- 
    
COMMIT;

SET AUTOCOMMIT TRUE;

