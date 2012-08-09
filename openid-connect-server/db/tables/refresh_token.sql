CREATE TABLE refresh_token (
	id VARCHAR(256),
	token_value VARCHAR(4096),
	expiration TIMESTAMP,
	client_id VARCHAR(256)
);