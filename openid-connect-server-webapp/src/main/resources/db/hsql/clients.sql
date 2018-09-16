--
-- Turn off autocommit and start a transaction so that we can use the temp tables
--

SET AUTOCOMMIT FALSE;

START TRANSACTION;

--
-- Insert client information into the temporary tables. To add clients to the HSQL database, edit things here.
-- 

INSERT INTO client_details (uuid, host_uuid, client_id, client_secret, client_name, dynamically_registered, refresh_token_validity_seconds, access_token_validity_seconds, id_token_validity_seconds, allow_introspection) VALUES
	('5fbe7dd6-2b5d-4d13-8096-a5f22d1dbf6d', '0629d968-4eb4-467d-b45f-f4b1a1d3e7f0', 'client', 'secret', 'Test Client', false, null, 3600, 600, true);

INSERT INTO client_scope (uuid, host_uuid, client_uuid, scope) VALUES
	('3e75c5f0-c26f-4f48-b871-d4b7ec3c03c1', '0629d968-4eb4-467d-b45f-f4b1a1d3e7f0', '5fbe7dd6-2b5d-4d13-8096-a5f22d1dbf6d', 'openid'),
	('b3137d93-7eb2-47f6-b01e-cc4b99dca2ea', '0629d968-4eb4-467d-b45f-f4b1a1d3e7f0', '5fbe7dd6-2b5d-4d13-8096-a5f22d1dbf6d', 'profile'),
	('b124f76c-00f4-4579-94dd-ad5d4640ca5d', '0629d968-4eb4-467d-b45f-f4b1a1d3e7f0', '5fbe7dd6-2b5d-4d13-8096-a5f22d1dbf6d', 'email'),
	('c5f1feac-7dad-4a71-95dd-eac58ff627ba', '0629d968-4eb4-467d-b45f-f4b1a1d3e7f0', '5fbe7dd6-2b5d-4d13-8096-a5f22d1dbf6d', 'address'),
	('6a27a293-2cde-40c0-93ab-aa80c3588b89', '0629d968-4eb4-467d-b45f-f4b1a1d3e7f0', '5fbe7dd6-2b5d-4d13-8096-a5f22d1dbf6d', 'phone'),
	('e1f841db-60ed-4caf-843e-222ed7c4b727', '0629d968-4eb4-467d-b45f-f4b1a1d3e7f0', '5fbe7dd6-2b5d-4d13-8096-a5f22d1dbf6d', 'offline_access');

INSERT INTO client_redirect_uri (uuid, host_uuid, client_uuid, redirect_uri) VALUES
	('244585f3-b09b-443b-8b47-1437c3c42b8c', '0629d968-4eb4-467d-b45f-f4b1a1d3e7f0', '5fbe7dd6-2b5d-4d13-8096-a5f22d1dbf6d', 'http://localhost/'),
	('34940097-1a0f-45cb-b2eb-997ef53ab5a7', '0629d968-4eb4-467d-b45f-f4b1a1d3e7f0', '5fbe7dd6-2b5d-4d13-8096-a5f22d1dbf6d', 'http://localhost:8080/');
	
INSERT INTO client_grant_type (uuid, host_uuid, client_uuid, grant_type) VALUES
	('83860c91-2a81-47f7-9e9f-aa2fc83b7663', '0629d968-4eb4-467d-b45f-f4b1a1d3e7f0', '5fbe7dd6-2b5d-4d13-8096-a5f22d1dbf6d', 'authorization_code'),
	('53b825c7-10d2-4d59-b08a-1e5eb289b1c4', '0629d968-4eb4-467d-b45f-f4b1a1d3e7f0', '5fbe7dd6-2b5d-4d13-8096-a5f22d1dbf6d', 'urn:ietf:params:oauth:grant_type:redelegate'),
	('560b5184-ce71-49a5-860c-546c68f09fc0', '0629d968-4eb4-467d-b45f-f4b1a1d3e7f0', '5fbe7dd6-2b5d-4d13-8096-a5f22d1dbf6d', 'urn:ietf:params:oauth:grant-type:device_code'),
	('2f07a959-8da9-43dc-b147-a752e20485c4', '0629d968-4eb4-467d-b45f-f4b1a1d3e7f0', '5fbe7dd6-2b5d-4d13-8096-a5f22d1dbf6d', 'implicit'),
	('0ec27ccd-c441-49c4-9cbe-4ab3cd71cecf', '0629d968-4eb4-467d-b45f-f4b1a1d3e7f0', '5fbe7dd6-2b5d-4d13-8096-a5f22d1dbf6d', 'refresh_token');
	

-- 
-- Close the transaction and turn autocommit back on
-- 
    
COMMIT;

SET AUTOCOMMIT TRUE;

