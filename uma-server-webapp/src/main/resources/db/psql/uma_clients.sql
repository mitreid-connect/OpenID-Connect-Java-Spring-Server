INSERT INTO client_details (uuid, host_uuid, client_id, client_secret, client_name, dynamically_registered, refresh_token_validity_seconds, access_token_validity_seconds, id_token_validity_seconds, allow_introspection) VALUES
	('1ad26bbc-b71a-444c-a197-806ca95b0619', '0629d968-4eb4-467d-b45f-f4b1a1d3e7f0', 'rs', 'secret', 'Test UMA RS', false, null, null, 600, false),
	('a279477e-d033-487d-8e47-3d2802ae1c9a', '0629d968-4eb4-467d-b45f-f4b1a1d3e7f0', 'c', 'secret', 'Test UMA Client', false, null, null, 600, false);

INSERT INTO client_scope (client_uuid, scope) VALUES
	('1ad26bbc-b71a-444c-a197-806ca95b0619', 'uma_protection'),
	('a279477e-d033-487d-8e47-3d2802ae1c9a', 'uma_authorization');

INSERT INTO client_grant_type (client_uuid, grant_type) VALUES
	('1ad26bbc-b71a-444c-a197-806ca95b0619', 'authorization_code'),
	('1ad26bbc-b71a-444c-a197-806ca95b0619', 'implicit'),
	('a279477e-d033-487d-8e47-3d2802ae1c9a', 'authorization_code'),
	('a279477e-d033-487d-8e47-3d2802ae1c9a', 'implicit');	