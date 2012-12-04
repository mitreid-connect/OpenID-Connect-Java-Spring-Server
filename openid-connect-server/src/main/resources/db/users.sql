--
-- Turn off autocommit and start a transaction so that we can use the temp tables
--

SET AUTOCOMMIT FALSE;

START TRANSACTION;

--
-- Insert user information into the temporary tables. To add users to the HSQL database, edit things here.
-- 

INSERT INTO users_TEMP (username, password, enabled) VALUES
 ('jricher', 'password', true),
 ('aanganes','password',true),
 ('mfranklin','password',true),
 ('srmoore','password',true);

INSERT INTO authorities_TEMP (username, authority) VALUES
  ('jricher', 'ROLE_ADMIN'),
  ('aanganes','ROLE_ADMIN'),
  ('jricher', 'ROLE_USER'),
  ('aanganes','ROLE_USER'),
  ('mfranklin','ROLE_USER'),
  ('srmoore','ROLE_USER');
    
-- By default, the user_id column here has to match the username column in the users table, above
INSERT INTO user_info_TEMP (user_id, preferred_username, name, email, email_verified) VALUES
  ('jricher', 'jricher', 'Justin Richer', 'jricher@mitre.org', false),
  ('jricher', 'aanganes', 'Amanda Anganes', 'aanganes@mitre.org', false),
  ('jricher', 'mfranklin', 'Matt Franklin', 'mfranklin@mitre.org', false),
  ('jricher', 'srmoore', 'Steve Moore', 'srmoore@mitre.org', false);
    
 
--
-- Merge the temporary users safely into the database. This is a two-step process to keep users from being created on every startup with a persistent store.
--

MERGE INTO users 
  USING (SELECT username, password, enabled FROM users_TEMP) AS vals(username, password, enabled)
  ON vals.username = users.username
  WHEN NOT MATCHED THEN 
    INSERT (username, password, enabled) VALUES(vals.username, vals.password, vals.enabled);

MERGE INTO authorities 
  USING (SELECT username, authority FROM authorities_TEMP) AS vals(username, authority)
  ON vals.username = authorities.username AND vals.authority = authorities.authority
  WHEN NOT MATCHED THEN 
    INSERT (username,authority) values (vals.username, vals.authority);

MERGE INTO user_info 
  USING (SELECT user_id, preferred_username, name, email, email_verified FROM user_info_TEMP) AS vals(user_id, preferred_username, name, email, email_verified)
  ON vals.preferred_username = user_info.preferred_username
  WHEN NOT MATCHED THEN 
    INSERT (user_id, preferred_username, name, email, email_verified) VALUES (vals.user_id, vals.preferred_username, vals.name, vals.email, vals.email_verified);

    
-- 
-- Close the transaction and turn autocommit back on
-- 
    
COMMIT;

SET AUTOCOMMIT TRUE;

