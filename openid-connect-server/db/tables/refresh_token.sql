CREATE TABLE refresh_token (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	token_value VARCHAR(4096),
	expiration TIMESTAMP,
	client_id VARCHAR(256)
);