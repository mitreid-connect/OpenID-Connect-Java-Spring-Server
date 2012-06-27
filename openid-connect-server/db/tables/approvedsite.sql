CREATE TABLE approvedsite (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	userinfo_id VARCHAR(256),
	clientdetails_id VARCHAR(256),
	creationDate DATE,
	accessDate DATE,
	timeoutDate DATE,
	whitelistedsite_id VARCHAR(256)
);