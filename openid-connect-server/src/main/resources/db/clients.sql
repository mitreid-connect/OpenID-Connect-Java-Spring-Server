--
-- Turn off autocommit and start a transaction so that we can use the temp tables
--

SET AUTOCOMMIT FALSE;

START TRANSACTION;

--
-- Insert client information into the temporary tables. To add clients to the HSQL database, edit things here.
-- 

INSERT INTO client_details_TEMP (client_id, client_secret, application_name, allow_refresh, dynamically_registered, refresh_token_validity_seconds, access_token_validity_seconds, id_token_validity_seconds) VALUES
	('client', 'secret', 'Test Client', true, false, null, 3600, 600);

INSERT INTO client_scope_TEMP (owner_id, scope) VALUES
	('client', 'openid'),
	('client', 'profile'),
	('client', 'email'),
	('client', 'address'),
	('client', 'phone'),
	('client', 'offline');

INSERT INTO redirect_uri_TEMP (owner_id, redirect_uri) VALUES
	('client', 'http://localhost/'),
	('client', 'http://localhost:8080/');
	
INSERT INTO authorized_grant_type_TEMP (owner_id, authorized_grant_type) VALUES
	('client', 'autorization_code'),
	('client', 'implicit');
	
--
-- Merge the temporary clients safely into the database. This is a two-step process to keep clients from being created on every startup with a persistent store.
--

MERGE INTO client_details 
  USING (SELECT client_id, client_secret, application_name, allow_refresh, dynamically_registered, refresh_token_validity_seconds, access_token_validity_seconds, id_token_validity_seconds FROM client_details_TEMP) AS vals(client_id, client_secret, application_name, allow_refresh, dynamically_registered, refresh_token_validity_seconds, access_token_validity_seconds, id_token_validity_seconds)
  ON vals.client_id = client_details.client_id
  WHEN NOT MATCHED THEN 
    INSERT (client_id, client_secret, application_name, allow_refresh, dynamically_registered, refresh_token_validity_seconds, access_token_validity_seconds, id_token_validity_seconds) VALUES(client_id, client_secret, application_name, allow_refresh, dynamically_registered, refresh_token_validity_seconds, access_token_validity_seconds, id_token_validity_seconds);

MERGE INTO client_scope 
  USING (SELECT id, scope FROM client_scope_TEMP, client_details WHERE client_details.client_id = client_scope_TEMP.owner_id) AS vals(id, scope)
  ON vals.id = client_scope.owner_id AND vals.scope = client_scope.scope
  WHEN NOT MATCHED THEN 
    INSERT (owner_id, scope) values (vals.id, vals.scope);

MERGE INTO redirect_uri 
  USING (SELECT id, redirect_uri FROM redirect_uri_TEMP, client_details WHERE client_details.client_id = redirect_uri_TEMP.owner_id) AS vals(id, redirect_uri)
  ON vals.id = redirect_uri.owner_id AND vals.redirect_uri = redirect_uri.redirect_uri
  WHEN NOT MATCHED THEN 
    INSERT (owner_id, redirect_uri) values (vals.id, vals.redirect_uri);

MERGE INTO authorized_grant_type 
  USING (SELECT id, authorized_grant_type FROM authorized_grant_type_TEMP, client_details WHERE client_details.client_id = authorized_grant_type_TEMP.owner_id) AS vals(id, authorized_grant_type)
  ON vals.id = authorized_grant_type.owner_id AND vals.authorized_grant_type = authorized_grant_type.authorized_grant_type
  WHEN NOT MATCHED THEN 
    INSERT (owner_id, authorized_grant_type) values (vals.id, vals.authorized_grant_type);
    
-- 
-- Close the transaction and turn autocommit back on
-- 
    
COMMIT;

SET AUTOCOMMIT TRUE;

