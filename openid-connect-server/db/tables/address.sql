CREATE TABLE address (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	formatted VARCHAR(256),
	streetAddress VARCHAR(256),
	locality VARCHAR(256),
	region VARCHAR(256),
	postalCode VARCHAR(256),
	country VARCHAR(256)
);