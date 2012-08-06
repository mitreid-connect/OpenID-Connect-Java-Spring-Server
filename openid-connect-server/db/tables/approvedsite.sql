CREATE TABLE approvedsite (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	userId VARCHAR(256),
	clientId VARCHAR(256),
	creationDate DATE,
	accessDate DATE,
	timeoutDate DATE,
	whitelistedsite_id VARCHAR(256)
);