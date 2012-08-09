CREATE TABLE whitelisted_site (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	creator_user_id VARCHAR(256),
	client_id VARCHAR(256)
);