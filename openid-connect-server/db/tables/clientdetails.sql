CREATE TABLE clientdetails (
	id VARCHAR(256),
	clientDescription VARCHAR(256),
	allowRefresh TINYINT,
	allowMultipleAccessTokens TINYINT,
	reuseRefreshTokens TINYINT,
	
	clientId VARCHAR(256),
	clientSecret VARCHAR(2000),
	accessTokenValiditySeconds BIGINT,
	refreshTokenValiditySeconds BIGINT,
	
	applicationType VARCHAR(256),
	applicationName VARCHAR(256),
	tokenEndpointAuthType VARCHAR(256),
	userIdType VARCHAR(256),
	
	logoUrl VARCHAR(256),
	policyUrl VARCHAR(256),
	jwkUrl VARCHAR(256),
	jwkEncryptionUrl VARCHAR(256),
	x509Url VARCHAR(256)
	x509EncryptionUrl VARCHAR(256),
	sectorIdentifierUrl VARCHAR(256),
	
	requreSignedRequestObject VARCHAR(256),
	
	userInfoSignedResponseAlg VARCHAR(256),
	userInfoEncryptedResponseAlg VARCHAR(256),
	userInfoEncryptedResponseEnc VARCHAR(256),
	userInfoEncryptedResponseInt VARCHAR(256),
	
	idTokenSignedResponseAlg VARCHAR(256),
	idTokenEncryptedResponseAlg VARCHAR(256),
	idTokenEncryptedResponseEnc VARCHAR(256),
	idTokenEncryptedResponseInt VARCHAR(256),
	
	defaultMaxAge BIGINT,
	requireAuthTime TINYINT,
	defaultACR VARCHAR(256)
);