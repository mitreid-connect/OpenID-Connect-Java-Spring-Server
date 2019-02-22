--
-- Insert client information into the temporary tables. To add clients to the Oracle database, edit things here.
-- 

INSERT INTO client_details_TEMP (client_id, client_secret, client_name, dynamically_registered, refresh_token_validity_seconds, access_token_validity_seconds, id_token_validity_seconds, allow_introspection) VALUES
	('client', 'secret', 'Test Client', 0, null, 3600, 600, 1);

INSERT INTO client_scope_TEMP (owner_id, scope) VALUES ('client', 'openid');
INSERT INTO client_scope_TEMP (owner_id, scope) VALUES ('client', 'profile');
INSERT INTO client_scope_TEMP (owner_id, scope) VALUES ('client', 'email');
INSERT INTO client_scope_TEMP (owner_id, scope) VALUES ('client', 'address');
INSERT INTO client_scope_TEMP (owner_id, scope) VALUES ('client', 'phone');
INSERT INTO client_scope_TEMP (owner_id, scope) VALUES ('client', 'offline_access');

INSERT INTO client_redirect_uri_TEMP (owner_id, redirect_uri) VALUES ('client', 'http://localhost/');
INSERT INTO client_redirect_uri_TEMP (owner_id, redirect_uri) VALUES ('client', 'http://localhost:8080/');

INSERT INTO client_grant_type_TEMP (owner_id, grant_type) VALUES ('client', 'authorization_code');
INSERT INTO client_grant_type_TEMP (owner_id, grant_type) VALUES ('client', 'urn:ietf:params:oauth:grant_type:redelegate');
INSERT INTO client_grant_type_TEMP (owner_id, grant_type) VALUES ('client', 'implicit');
INSERT INTO client_grant_type_TEMP (owner_id, grant_type) VALUES ('client', 'refresh_token');
	
--
-- Merge the temporary clients safely into the database. This is a two-step process to keep clients from being created on every startup with a persistent store.
--

MERGE INTO client_details 
  USING (SELECT client_id, client_secret, client_name, dynamically_registered, refresh_token_validity_seconds, access_token_validity_seconds, id_token_validity_seconds, allow_introspection FROM client_details_TEMP) vals
  ON (vals.client_id = client_details.client_id)
  WHEN NOT MATCHED THEN 
    INSERT (id, client_id, client_secret, client_name, dynamically_registered, refresh_token_validity_seconds, access_token_validity_seconds,
    id_token_validity_seconds, allow_introspection) VALUES(client_details_seq.nextval, vals.client_id, vals.client_secret, vals.client_name, vals.dynamically_registered,
    vals.refresh_token_validity_seconds, vals.access_token_validity_seconds, vals.id_token_validity_seconds, vals.allow_introspection);

MERGE INTO client_scope 
  USING (SELECT id, scope FROM client_scope_TEMP, client_details WHERE client_details.client_id = client_scope_TEMP.owner_id) vals
  ON (vals.id = client_scope.owner_id AND vals.scope = client_scope.scope)
  WHEN NOT MATCHED THEN 
    INSERT (owner_id, scope) values (vals.id, vals.scope);

MERGE INTO client_redirect_uri 
  USING (SELECT id, redirect_uri FROM client_redirect_uri_TEMP, client_details WHERE client_details.client_id = client_redirect_uri_TEMP.owner_id) vals
  ON (vals.id = client_redirect_uri.owner_id AND vals.redirect_uri = client_redirect_uri.redirect_uri)
  WHEN NOT MATCHED THEN 
    INSERT (owner_id, redirect_uri) values (vals.id, vals.redirect_uri);

MERGE INTO client_grant_type 
  USING (SELECT id, grant_type FROM client_grant_type_TEMP, client_details WHERE client_details.client_id = client_grant_type_TEMP.owner_id) vals
  ON (vals.id = client_grant_type.owner_id AND vals.grant_type = client_grant_type.grant_type)
  WHEN NOT MATCHED THEN 
    INSERT (owner_id, grant_type) values (vals.id, vals.grant_type);
