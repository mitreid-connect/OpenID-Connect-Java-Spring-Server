CREATE TABLE refreshtoken (
	id VARCHAR(256),
	tokenValue VARCHAR(4096),
	expiration TIMESTAMP,
	client_id VARCHAR(256)
);