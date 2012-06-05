CREATE TABLE clientdetails (
	clientId VARCHAR(256),
	clientSecret VARCHAR(2000),
	clientName VARCHAR(256),
	clientDescription VARCHAR(2000),
	allowRefresh TINYINT,
	accessTokenTimeout BIGINT,
	refreshTokenTimeout BIGINT,
	owner VARCHAR(256)
);