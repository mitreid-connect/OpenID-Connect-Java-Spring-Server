CREATE TABLE accesstoken (
	id VARCHAR(4096),
	expiration TIMESTAMP,
	tokenType VARCHAR(256),
	refresh_token_id VARCHAR(256),
	client_id VARCHAR(256),
	authentication LONGBLOB,
	idTokenString VARCHAR(4096)
);
CREATE TABLE address (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	formatted VARCHAR(256),
	streetAddress VARCHAR(256),
	locality VARCHAR(256),
	region VARCHAR(256),
	postalCode VARCHAR(256),
	country VARCHAR(256)
);
CREATE TABLE approvedsite (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	userinfo_id VARCHAR(256),
	clientdetails_id VARCHAR(256),
	creationDate DATE,
	accessDate DATE,
	timeoutDate DATE
);
CREATE TABLE authorities (
	owner_id VARCHAR(256),
	authorities LONGBLOB
);
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
CREATE TABLE event (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	type INT(3),
	timestamp DATE
);
CREATE TABLE authorizedgranttypes (
	owner_id VARCHAR(256),
	authorizedgranttypes VARCHAR(2000)
);
CREATE TABLE idtoken (
	id BIGINT AUTO_INCREMENT PRIMARY KEY
);
CREATE TABLE idtokenclaims (
	id BIGINT AUTO_INCREMENT PRIMARY KEY
);
CREATE TABLE refreshtoken (
	id VARCHAR(256),
	expiration TIMESTAMP,
	client_id VARCHAR(256)
);
CREATE TABLE resource_ids (
	owner_id VARCHAR(256), 
	resourceids VARCHAR(256) 
);
CREATE TABLE scope (
	owner_id VARCHAR(256),
	scope VARCHAR(2000)
);
CREATE TABLE userinfo (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	userId VARCHAR(256),
	name VARCHAR(256),
	givenName VARCHAR(256),
	familyName VARCHAR(256),
	middleName VARCHAR(256),
	nickname VARCHAR(256),
	profile VARCHAR(256),
	picture VARCHAR(256),
	website VARCHAR(256),
	email VARCHAR(256),
	verified BOOLEAN,
	gender VARCHAR(256),
	zoneinfo VARCHAR(256),
	locale VARCHAR(256),
	phoneNumber VARCHAR(256),
	address_id VARCHAR(256),
	updatedTime VARCHAR(256)
);
CREATE TABLE whitelistedsite (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	userinfo_id VARCHAR(256),
	clientdetails_id VARCHAR(256)
);