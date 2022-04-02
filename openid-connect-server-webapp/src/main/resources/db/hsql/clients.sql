--
-- Turn off autocommit and start a transaction so that we can use the temp tables
--

SET AUTOCOMMIT FALSE;

START TRANSACTION;

--
-- Insert client information into the temporary tables. To add clients to the HSQL database, edit things here.
-- 

INSERT INTO client_details (client_id, client_secret, client_name, dynamically_registered, refresh_token_validity_seconds, access_token_validity_seconds, id_token_validity_seconds, allow_introspection, jwks, token_endpoint_auth_method) VALUES
	('client', 'secret', 'Test Client', false, null, 3600, 600, true, 
	'{"keys": [{ "kty": "RSA", "d": "m1M7uj1uZMgQqd2qwqBk07rgFzbzdCAbsfu5kvqoALv3oRdyi_UVHXDhos3DZVQ3M6mKgb30XXESykY8tpWcQOU-qx6MwtSFbo-3SNx9fBtylyQosHECGyleVP79YTE4mC0odRoUIDS90J9AcFsdVtC6M2oJ3CCL577a-lJg6eYyQoRmbjdzqMnBFJ99TCfR6wBQQbzXi1K_sN6gcqhxMmQXHWlqfT7-AJIxX9QUF0rrXMMX9fPh-HboGKs2Dqoo3ofJ2XuePpmpVDvtGy_jenXmUdpsRleqnMrEI2qkBonJQSKL4HPNpsylbQyXt2UtYrzcopCp7jL-j56kRPpQAQ", "e": "AQAB",  "kid": "xyz-client",  "alg": "RS256", "n": "zwCT_3bx-glbbHrheYpYpRWiY9I-nEaMRpZnRrIjCs6b_emyTkBkDDEjSysi38OC73hj1-WgxcPdKNGZyIoH3QZen1MKyyhQpLJG1-oLNLqm7pXXtdYzSdC9O3-oiyy8ykO4YUyNZrRRfPcihdQCbO_OC8Qugmg9rgNDOSqppdaNeas1ov9PxYvxqrz1-8Ha7gkD00YECXHaB05uMaUadHq-O_WIvYXicg6I5j6S44VNU65VBwu-AlynTxQdMAWP3bYxVVy6p3-7eTJokvjYTFqgDVDZ8lUXbr5yCTnRhnhJgvf3VjD_malNe8-tOqK5OSDlHTy6gD9NqdGCm-Pm3Q" }]}',
	'PRIVATE_KEY');

INSERT INTO client_scope (owner_id, scope) VALUES
	(1, 'openid'),
	(1, 'profile'),
	(1, 'email'),
	(1, 'address'),
	(1, 'phone'),
	(1, 'offline_access');

INSERT INTO client_redirect_uri (owner_id, redirect_uri) VALUES
	(1, 'http://localhost/'),
	(1, 'http://localhost:8080/'),
	(1, 'http://host.docker.internal:9834/api/client/callback');
	
INSERT INTO client_grant_type (owner_id, grant_type) VALUES
	(1, 'authorization_code'),
	(1, 'urn:ietf:params:oauth:grant_type:redelegate'),
	(1, 'urn:ietf:params:oauth:grant-type:device_code'),
	(1, 'implicit'),
	(1, 'refresh_token');
	
COMMIT;

SET AUTOCOMMIT TRUE;

