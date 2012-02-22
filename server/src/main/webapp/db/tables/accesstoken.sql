CREATE TABLE accesstoken (
	id VARCHAR(256),
	expiration TIMESTAMP,
	tokenType VARCHAR(256),
	refresh_token_id VARCHAR(256),
	client_id VARCHAR(256),
	authentication LONGBLOB,
	idTokenString VARCHAR(256)
);