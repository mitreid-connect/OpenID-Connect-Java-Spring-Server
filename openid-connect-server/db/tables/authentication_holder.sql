CREATE TABLE authentication_holder (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	owner_id VARCHAR(256),
	authentication LONGBLOB
);