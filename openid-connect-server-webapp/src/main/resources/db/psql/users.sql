--
-- Turn off autocommit and start a transaction so that we can use the temp tables
--

--SET AUTOCOMMIT FALSE;

START TRANSACTION;

--
-- Insert user information into the temporary tables. To add users to the HSQL database, edit things here.
-- 

INSERT INTO users_TEMP (username, password, enabled) VALUES
  ('admin','password',true),
  ('user','password',true);


INSERT INTO authorities_TEMP (username, authority) VALUES
  ('admin','ROLE_ADMIN'),
  ('admin','ROLE_USER'),
  ('user','ROLE_USER');
    
-- By default, the username column here has to match the username column in the users table, above
INSERT INTO user_info_TEMP (sub, preferred_username, name, email, email_verified) VALUES
  ('90342.ASDFJWFA','admin','Demo Admin','admin@example.com', true),
  ('01921.FLANRJQW','user','Demo User','user@example.com', true);

 
--
-- Merge the temporary users safely into the database. This is a two-step process to keep users from being created on every startup with a persistent store.
--

INSERT INTO users
  SELECT username, password, enabled FROM users_TEMP
  ON CONFLICT(username)
  DO NOTHING;

INSERT INTO authorities
  SELECT username, authority FROM authorities_TEMP
  ON CONFLICT(username, authority)
  DO NOTHING;

INSERT INTO user_info (sub, preferred_username, name, email, email_verified)
  SELECT sub, preferred_username, name, email, email_verified FROM user_info_TEMP
  ON CONFLICT
  DO NOTHING;
    
-- 
-- Close the transaction and turn autocommit back on
-- 
    
COMMIT;

--SET AUTOCOMMIT TRUE;

