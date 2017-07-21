--
-- Turn off autocommit and start a transaction so that we can use the temp tables
--

SET AUTOCOMMIT FALSE;

START TRANSACTION;

--
-- Insert client information into the temporary tables. To add clients to the HSQL database, edit things here.
-- 

INSERT INTO client_details_TEMP (client_id, client_secret, client_name, dynamically_registered, refresh_token_validity_seconds, access_token_validity_seconds, id_token_validity_seconds, allow_introspection) VALUES
	('client', 'secret', 'Facebook', false, null, 3600, 600, true);

INSERT INTO client_scope_TEMP (owner_id, scope) VALUES
	('client', 'openid'),
	('client', 'profile'),
	('client', 'email'),
	('client', 'address'),
	('client', 'phone'),
	('client', 'offline_access');

INSERT INTO client_redirect_uri_TEMP (owner_id, redirect_uri) VALUES
	('client', 'http://localhost/'),
	('client', 'http://localhost:8080/'),
        ('client', 'http://localhost/jsApp/popup.html');
	
INSERT INTO client_grant_type_TEMP (owner_id, grant_type) VALUES
	('client', 'authorization_code'),
	('client', 'urn:ietf:params:oauth:grant_type:redelegate'),
	('client', 'urn:ietf:params:oauth:grant-type:device_code'),
	('client', 'implicit'),
	('client', 'refresh_token');

INSERT INTO client_details_TEMP (client_id, client_secret, client_name, dynamically_registered, refresh_token_validity_seconds, access_token_validity_seconds, id_token_validity_seconds, allow_introspection) VALUES
	('client2', 'secret2', 'Twitter', false, null, 3600, 600, true);

INSERT INTO client_scope_TEMP (owner_id, scope) VALUES
	('client2', 'openid'),
	('client2', 'profile'),
	('client2', 'email'),
	('client2', 'address'),
	('client2', 'phone'),
	('client2', 'offline_access');

INSERT INTO client_redirect_uri_TEMP (owner_id, redirect_uri) VALUES
	('client2', 'http://localhost/'),
	('client2', 'http://localhost:8080/'),
        ('client2', 'http://localhost/jsApp/popup.html');
	
INSERT INTO client_grant_type_TEMP (owner_id, grant_type) VALUES
	('client2', 'authorization_code'),
	('client2', 'urn:ietf:params:oauth:grant_type:redelegate'),
	('client2', 'urn:ietf:params:oauth:grant-type:device_code'),
	('client2', 'implicit'),
	('client2', 'refresh_token');

INSERT INTO client_details_TEMP (client_id, client_secret, client_name, dynamically_registered, refresh_token_validity_seconds, access_token_validity_seconds, id_token_validity_seconds, allow_introspection) VALUES
	('client3', 'secret3', 'IOS', false, null, 3600, 600, true);

INSERT INTO client_scope_TEMP (owner_id, scope) VALUES
	('client3', 'openid'),
	('client3', 'profile'),
	('client3', 'email'),
	('client3', 'address'),
	('client3', 'phone'),
	('client3', 'offline_access');

INSERT INTO client_redirect_uri_TEMP (owner_id, redirect_uri) VALUES
	('client3', 'http://localhost/'),
	('client3', 'http://localhost:8080/'),
        ('client3', 'http://localhost/jsApp/popup.html');
	
INSERT INTO client_grant_type_TEMP (owner_id, grant_type) VALUES
	('client3', 'authorization_code'),
	('client3', 'urn:ietf:params:oauth:grant_type:redelegate'),
	('client3', 'urn:ietf:params:oauth:grant-type:device_code'),
	('client3', 'implicit'),
	('client3', 'refresh_token');
	
--
-- Merge the temporary clients safely into the database. This is a two-step process to keep clients from being created on every startup with a persistent store.
--

MERGE INTO client_details 
  USING (SELECT client_id, client_secret, client_name, dynamically_registered, refresh_token_validity_seconds, access_token_validity_seconds, id_token_validity_seconds, allow_introspection FROM client_details_TEMP) AS vals(client_id, client_secret, client_name, dynamically_registered, refresh_token_validity_seconds, access_token_validity_seconds, id_token_validity_seconds, allow_introspection)
  ON vals.client_id = client_details.client_id
  WHEN NOT MATCHED THEN 
    INSERT (client_id, client_secret, client_name, dynamically_registered, refresh_token_validity_seconds, access_token_validity_seconds, id_token_validity_seconds, allow_introspection) VALUES(client_id, client_secret, client_name, dynamically_registered, refresh_token_validity_seconds, access_token_validity_seconds, id_token_validity_seconds, allow_introspection);

MERGE INTO client_scope 
  USING (SELECT id, scope FROM client_scope_TEMP, client_details WHERE client_details.client_id = client_scope_TEMP.owner_id) AS vals(id, scope)
  ON vals.id = client_scope.owner_id AND vals.scope = client_scope.scope
  WHEN NOT MATCHED THEN 
    INSERT (owner_id, scope) values (vals.id, vals.scope);

MERGE INTO client_redirect_uri 
  USING (SELECT id, redirect_uri FROM client_redirect_uri_TEMP, client_details WHERE client_details.client_id = client_redirect_uri_TEMP.owner_id) AS vals(id, redirect_uri)
  ON vals.id = client_redirect_uri.owner_id AND vals.redirect_uri = client_redirect_uri.redirect_uri
  WHEN NOT MATCHED THEN 
    INSERT (owner_id, redirect_uri) values (vals.id, vals.redirect_uri);

MERGE INTO client_grant_type 
  USING (SELECT id, grant_type FROM client_grant_type_TEMP, client_details WHERE client_details.client_id = client_grant_type_TEMP.owner_id) AS vals(id, grant_type)
  ON vals.id = client_grant_type.owner_id AND vals.grant_type = client_grant_type.grant_type
  WHEN NOT MATCHED THEN 
    INSERT (owner_id, grant_type) values (vals.id, vals.grant_type);
    
-- 
-- Close the transaction and turn autocommit back on
-- 
    
COMMIT;

SET AUTOCOMMIT TRUE;

