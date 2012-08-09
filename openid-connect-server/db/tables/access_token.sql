CREATE TABLE access_token (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	token_value VARCHAR(4096),
	expiration TIMESTAMP,
	token_type VARCHAR(256),
	refresh_token_id VARCHAR(256),
	client_id VARCHAR(256),
	auth_holder_id VARCHAR(256),
	id_token_string VARCHAR(4096)
);