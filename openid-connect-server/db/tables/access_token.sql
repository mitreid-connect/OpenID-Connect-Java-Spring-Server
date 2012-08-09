CREATE TABLE access_token (
	id VARCHAR(256),
	token_value VARCHAR(4096),
	expiration TIMESTAMP,
	token_type VARCHAR(256),
	refresh_token_id VARCHAR(256),
	client_id VARCHAR(256),
	authentication LONGBLOB,
	id_token_string VARCHAR(4096)
);