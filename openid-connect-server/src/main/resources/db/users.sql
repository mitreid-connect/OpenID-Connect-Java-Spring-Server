SET AUTOCOMMIT FALSE;

START TRANSACTION;

MERGE INTO users 
  USING (VALUES ('jricher','password',true)) AS vals(username, password, enabled)
  ON vals.username = users.username
  WHEN NOT MATCHED THEN 
    INSERT (username, password, enabled) VALUES(vals.username, vals.password, vals.enabled);

CREATE TEMPORARY TABLE authorities_TEMP (
      username varchar(50) not null,
      authority varchar(50) not null,
      constraint ix_authority_TEMP unique (username,authority));
      
INSERT INTO authorities_TEMP (username, authority) VALUES
  ('jricher', 'ROLE_USER'),
  ('jricher', 'ROLE_ADMIN'),
  ('jricher', 'ROLE_AWESOME');
    
MERGE INTO authorities 
--  USING (VALUES ('jricher', CAST('ROLE_USER' AS varchar(50))), ('jricher', CAST('ROLE_ADMIN' AS varchar(50))), ('jricher', CAST('ROLE_AWESOME' AS varchar(50)))) AS vals(username, authority)
--  USING (VALUES ('jricher', 'ROLE_USER'), ('jricher', 'ROLE_ADMIN'), ('jricher', 'ROLE_AWESOME')) AS vals(username varchar(50), authority varchar(50))
  USING (SELECT username, authority FROM authorities_TEMP) AS vals(username, authority)
  ON vals.username = authorities.username AND vals.authority = authorities.authority
  WHEN NOT MATCHED THEN 
    INSERT (username,authority) values (vals.username, vals.authority);

DROP TABLE authorities_TEMP;
    
--INSERT INTO authorities (username, authority) VALUES ('jricher', 'ROLE_USER'), ('jricher', 'ROLE_ADMIN');    

MERGE INTO user_info 
  USING (VALUES('user1-abc123', 'jricher', 'Justin Richer', false)) AS vals(user_id, preferred_username, name, email_verified)
  ON vals.preferred_username = user_info.preferred_username
  WHEN NOT MATCHED THEN 
    INSERT (user_id, preferred_username, name, email_verified) VALUES (vals.user_id, vals.preferred_username, vals.name, vals.email_verified);
    
COMMIT;

SET AUTOCOMMIT TRUE;

--INSERT INTO users(username, password, enabled) values ('aanganes','password',true) where not exists (select * from user_info where username='aanganes');
--INSERT INTO authorities(username,authority) values ('aanganes','ROLE_USER') where not exists (select * from user_info where username='aanganes');
--INSERT INTO authorities(username,authority) values ('aanganes','ROLE_ADMIN') where not exists (select * from user_info where username='aanganes');
--INSERT INTO user_info(user_id, preferred_username, name, email_verified) values ('aanganes','aanganes','aanganes', 'FALSE') where not exists (select * from user_info where username='aanganes');
--
--INSERT INTO users(username, password, enabled) values ('mfranklin','password',true) where not exists (select * from user_info where username='mfranklin');
--INSERT INTO authorities(username,authority) values ('mfranklin','ROLE_USER') where not exists (select * from user_info where username='mfranklin');
--INSERT INTO user_info(user_id, preferred_username, name, email_verified) values ('mfranklin','mfranklin','mfranklin', 'FALSE') where not exists (select * from user_info where username='mfranklin');
--
--INSERT INTO users(username, password, enabled) values ('srmoore','password',true) where not exists (select * from user_info where username='srmoore');
--INSERT INTO authorities(username,authority) values ('srmoore','ROLE_USER') where not exists (select * from user_info where username='srmoore');
--INSERT INTO user_info(user_id, preferred_username, name, email_verified) values ('srmoore','srmoore','srmoore', 'FALSE') where not exists (select * from user_info where username='srmoore');
