CREATE TABLE clientdetails (
	clientId VARCHAR(256),
	clientSecret VARCHAR(2000),
	registeredRedirectUri VARCHAR(2000),
	clientName VARCHAR(256),
	clientDescription VARCHAR(2000),
	allowRefresh TINYINT,
	accessTokenTimeout BIGINT,
	refreshTokenTimeout BIGINT,
	owner VARCHAR(256)
);
-- TODO len of registeredRedirectUri VARCHAR(2000) does not match the one in redirect_uris.sql 
-- which is registeredRedirectUri VARCHAR(256) should this be a foriegn key constraint?

-- TODO allowRefresh is a boolean in the code, should be specfied here