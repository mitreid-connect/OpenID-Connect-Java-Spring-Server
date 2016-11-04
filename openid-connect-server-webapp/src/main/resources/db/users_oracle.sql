--
-- Insert user information into the temporary tables. To add users to the Oracle database, edit things here.
-- 

INSERT INTO users_TEMP (username, password, enabled) VALUES ('admin','password',1);
INSERT INTO users_TEMP (username, password, enabled) VALUES ('user','password',1);


INSERT INTO authorities_TEMP (username, authority) VALUES ('admin','ROLE_ADMIN');
INSERT INTO authorities_TEMP (username, authority) VALUES('admin','ROLE_USER');
INSERT INTO authorities_TEMP (username, authority) VALUES('user','ROLE_USER');
    
-- By default, the username column here has to match the username column in the users table, above
INSERT INTO user_info_TEMP (sub, preferred_username, name, email, email_verified) VALUES ('90342.ASDFJWFA','admin','Demo Admin','admin@example.com', 1);
INSERT INTO user_info_TEMP (sub, preferred_username, name, email, email_verified) VALUES ('01921.FLANRJQW','user','Demo User','user@example.com', 1);

 
--
-- Merge the temporary users safely into the database. This is a two-step process to keep users from being created on every startup with a persistent store.
--

MERGE INTO users 
  USING (SELECT username, password, enabled FROM users_TEMP) vals
  ON (vals.username = users.username)
  WHEN NOT MATCHED THEN 
    INSERT (username, password, enabled) VALUES(vals.username, vals.password, vals.enabled);

MERGE INTO authorities 
  USING (SELECT username, authority FROM authorities_TEMP) vals
  ON (vals.username = authorities.username AND vals.authority = authorities.authority)
  WHEN NOT MATCHED THEN 
    INSERT (username,authority) values (vals.username, vals.authority);

MERGE INTO user_info 
  USING (SELECT sub, preferred_username, name, email, email_verified FROM user_info_TEMP) vals
  ON (vals.preferred_username = user_info.preferred_username)
  WHEN NOT MATCHED THEN 
    INSERT (id, sub, preferred_username, name, email, email_verified) VALUES (user_info_seq.nextval, vals.sub, vals.preferred_username, vals.name, vals.email,
    vals.email_verified);
