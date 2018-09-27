--
-- Turn off autocommit and start a transaction so that we can use the temp tables
--

SET AUTOCOMMIT = 0;

START TRANSACTION;


INSERT INTO client_details (uuid, host_uuid, client_id, client_secret, client_name, dynamically_registered, refresh_token_validity_seconds, access_token_validity_seconds, id_token_validity_seconds, allow_introspection) VALUES
	('5fbe7dd6-2b5d-4d13-8096-a5f22d1dbf6d', '0629d968-4eb4-467d-b45f-f4b1a1d3e7f0', 'client', 'secret', 'Test Client', false, null, 3600, 600, true);

INSERT INTO client_scope (client_uuid, scope) VALUES
	('5fbe7dd6-2b5d-4d13-8096-a5f22d1dbf6d', 'openid'),
	('5fbe7dd6-2b5d-4d13-8096-a5f22d1dbf6d', 'profile'),
	('5fbe7dd6-2b5d-4d13-8096-a5f22d1dbf6d', 'email'),
	('5fbe7dd6-2b5d-4d13-8096-a5f22d1dbf6d', 'address'),
	('5fbe7dd6-2b5d-4d13-8096-a5f22d1dbf6d', 'phone'),
	('5fbe7dd6-2b5d-4d13-8096-a5f22d1dbf6d', 'offline_access');

INSERT INTO client_redirect_uri (client_uuid, redirect_uri) VALUES
	('5fbe7dd6-2b5d-4d13-8096-a5f22d1dbf6d', 'http://localhost/'),
	('5fbe7dd6-2b5d-4d13-8096-a5f22d1dbf6d', 'http://localhost:8080/');
	
INSERT INTO client_grant_type (client_uuid, grant_type) VALUES
	('5fbe7dd6-2b5d-4d13-8096-a5f22d1dbf6d', 'authorization_code'),
	('5fbe7dd6-2b5d-4d13-8096-a5f22d1dbf6d', 'urn:ietf:params:oauth:grant_type:redelegate'),
	('5fbe7dd6-2b5d-4d13-8096-a5f22d1dbf6d', 'urn:ietf:params:oauth:grant-type:device_code'),
	('5fbe7dd6-2b5d-4d13-8096-a5f22d1dbf6d', 'implicit'),
	('5fbe7dd6-2b5d-4d13-8096-a5f22d1dbf6d', 'refresh_token');
	

COMMIT;

SET AUTOCOMMIT = 1;
