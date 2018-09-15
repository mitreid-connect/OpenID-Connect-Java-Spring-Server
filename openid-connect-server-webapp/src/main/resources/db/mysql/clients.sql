--
-- Turn off autocommit and start a transaction so that we can use the temp tables
--

SET AUTOCOMMIT = 0;

START TRANSACTION;

--
-- Insert client information into the temporary tables. To add clients to the HSQL database, edit things here.
-- 

CREATE TEMPORARY TABLE client_details_TEMP SELECT CONVERT('client', CHAR(255) CHARACTER SET utf8) as client_id, CONVERT('secret', CHAR(255) CHARACTER SET utf8) as client_secret, CONVERT('Test Client', CHAR(255) CHARACTER SET utf8) as client_name, false as dynamically_registered, null as refresh_token_validity_seconds, 3600 as access_token_validity_seconds, 600 as id_token_validity_seconds, true as allow_introspection;

CREATE TEMPORARY TABLE client_scope_TEMP SELECT CONVERT('client', CHAR(255) CHARACTER SET utf8) as owner_id, CONVERT('openid', CHAR(255) CHARACTER SET utf8) as scope;

INSERT INTO client_scope_TEMP (owner_id, scope) VALUES
	('client', 'profile'),
	('client', 'email'),
	('client', 'address'),
	('client', 'phone'),
	('client', 'offline_access');


CREATE TEMPORARY TABLE 	client_redirect_uri_TEMP SELECT CONVERT('client', CHAR(255) CHARACTER SET utf8) AS owner_id, CONVERT('http://localhost/', CHAR(255) CHARACTER SET utf8) AS redirect_uri;

INSERT INTO client_redirect_uri_TEMP (owner_id, redirect_uri) VALUES
	('client', 'http://localhost:8080/');
	
	
CREATE TEMPORARY TABLE client_grant_type_TEMP SELECT  CONVERT('client', CHAR(255) CHARACTER SET utf8) as owner_id,  CONVERT('authorization_code', CHAR(255) CHARACTER SET utf8) as grant_type;


INSERT INTO client_grant_type_TEMP (owner_id, grant_type) VALUES
	('client', 'urn:ietf:params:oauth:grant_type:redelegate'),
	('client', 'implicit'),
	('client', 'refresh_token');
	
--
-- Merge the temporary clients safely into the database. This is a two-step process to keep clients from being created on every startup with a persistent store.
--

INSERT INTO client_details (client_id, client_secret, client_name, dynamically_registered, refresh_token_validity_seconds, access_token_validity_seconds, id_token_validity_seconds, allow_introspection)
  SELECT client_id, client_secret, client_name, dynamically_registered, refresh_token_validity_seconds, access_token_validity_seconds, id_token_validity_seconds, allow_introspection FROM client_details_TEMP
  ON DUPLICATE KEY UPDATE client_details.client_id = client_details.client_id;

INSERT INTO client_scope (owner_id, scope)
  SELECT id, scope FROM client_scope_TEMP, client_details WHERE client_details.client_id = client_scope_TEMP.owner_id
  ON DUPLICATE KEY UPDATE client_scope.owner_id = client_scope.owner_id;

INSERT INTO client_redirect_uri (owner_id, redirect_uri)
  SELECT id, redirect_uri FROM client_redirect_uri_TEMP, client_details WHERE client_details.client_id = client_redirect_uri_TEMP.owner_id
  ON DUPLICATE KEY UPDATE client_redirect_uri.owner_id = client_redirect_uri.owner_id;

INSERT INTO client_grant_type (owner_id, grant_type)
  SELECT id, grant_type FROM client_grant_type_TEMP, client_details WHERE client_details.client_id = client_grant_type_TEMP.owner_id
  ON DUPLICATE KEY UPDATE client_grant_type.owner_id = client_grant_type.owner_id;
    
-- 
-- Close the transaction and turn autocommit back on
-- 
    

COMMIT;

SET AUTOCOMMIT = 1;


DROP TEMPORARY TABLE client_details_TEMP;
DROP TEMPORARY TABLE client_scope_TEMP;
DROP TEMPORARY TABLE client_redirect_uri_TEMP;
DROP TEMPORARY TABLE client_grant_type_TEMP;
