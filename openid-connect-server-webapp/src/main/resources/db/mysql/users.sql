--
-- Turn off autocommit and start a transaction so that we can use the temp tables
--

SET AUTOCOMMIT = 0;

START TRANSACTION;

--
-- Insert user information into the temporary tables. To add users to the HSQL database, edit things here.
-- 

CREATE TEMPORARY TABLE users_TEMP SELECT CONVERT('admin', CHAR(255) CHARACTER SET utf8) as username, CONVERT('password', CHAR(255) CHARACTER SET utf8) as password, true as enabled;

INSERT INTO users_TEMP (username, password, enabled) VALUES
  ('user','password',true);

  
CREATE TEMPORARY TABLE authorities_TEMP SELECT CONVERT('admin', CHAR(255) CHARACTER SET utf8) as username, CONVERT('ROLE_ADMIN', CHAR(255) CHARACTER SET utf8) as authority;

INSERT INTO authorities_TEMP (username, authority) VALUES
  ('admin','ROLE_USER'),
  ('user','ROLE_USER');
    
  
CREATE TEMPORARY TABLE user_info_TEMP SELECT CONVERT('90342.ASDFJWFA', CHAR(255) CHARACTER SET utf8) as sub, CONVERT('admin', CHAR(255) CHARACTER SET utf8) as preferred_username, CONVERT('Demo Admin', CHAR(255) CHARACTER SET utf8) as name, CONVERT('admin@example.com', CHAR(255) CHARACTER SET utf8) as email, true as email_verified;

-- By default, the username column here has to match the username column in the users table, above
INSERT INTO user_info_TEMP (sub, preferred_username, name, email, email_verified) VALUES
  ('01921.FLANRJQW','user','Demo User','user@example.com', true);

 
--
-- Merge the temporary users safely into the database. This is a two-step process to keep users from being created on every startup with a persistent store.
--

INSERT INTO users (username, password, enabled)
  SELECT username, password, enabled FROM users_TEMP
  ON DUPLICATE KEY UPDATE users.username = users.username;

INSERT INTO authorities (username,authority)
  SELECT username, authority FROM authorities_TEMP
  ON DUPLICATE KEY UPDATE authorities.username = authorities.username;

INSERT INTO user_info (sub, preferred_username, name, email, email_verified)
  SELECT sub, preferred_username, name, email, email_verified FROM user_info_TEMP
  ON DUPLICATE KEY UPDATE user_info.preferred_username = user_info.preferred_username;
    
-- 
-- Close the transaction and turn autocommit back on
-- 


COMMIT;

SET AUTOCOMMIT = 1;

DROP TEMPORARY TABLE users_TEMP;
    
DROP TEMPORARY TABLE authorities_TEMP;

DROP TEMPORARY TABLE user_info_TEMP;