--
-- Tables for OIDC Server functionality, HSQL
--

CREATE TABLE IF NOT EXISTS access_token (
	id BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY,
	token_value VARCHAR(4096),
	expiration TIMESTAMP,
	token_type VARCHAR(256),
	refresh_token_id BIGINT,
	client_id VARCHAR(256),
	auth_holder_id BIGINT,
	id_token_id BIGINT,
	approved_site_id BIGINT
);

CREATE TABLE IF NOT EXISTS address (
	id BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY,
	formatted VARCHAR(256),
	street_address VARCHAR(256),
	locality VARCHAR(256),
	region VARCHAR(256),
	postal_code VARCHAR(256),
	country VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS approved_site (
	id BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY,
	user_id VARCHAR(4096),
	client_id VARCHAR(4096),
	creation_date TIMESTAMP,
	access_date TIMESTAMP,
	timeout_date TIMESTAMP,
	whitelisted_site_id VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS approved_site_scope (
	owner_id BIGINT,
	scope VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS authentication_holder (
	id BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY,
	owner_id BIGINT,
	authentication LONGVARBINARY
);

CREATE TABLE IF NOT EXISTS client_authority (
	owner_id BIGINT,
	authority LONGVARBINARY
);

CREATE TABLE IF NOT EXISTS authorization_code (
	id BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY,
	code VARCHAR(256),
	authentication LONGVARBINARY
);

CREATE TABLE IF NOT EXISTS client_grant_type (
	owner_id BIGINT,
	grant_type VARCHAR(2000)
);

CREATE TABLE IF NOT EXISTS client_response_type (
	owner_id BIGINT,
	response_type VARCHAR(2000)
);

CREATE TABLE IF NOT EXISTS blacklisted_site (
	id BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY,
	uri VARCHAR(2048)
);

CREATE TABLE IF NOT EXISTS client_details (
	id BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY,

	client_description VARCHAR(1024),
	reuse_refresh_tokens BOOLEAN NOT NULL DEFAULT true,
	dynamically_registered BOOLEAN NOT NULL DEFAULT false,
	trusted_registration BOOLEAN NOT NULL DEFAULT false,
	allow_introspection BOOLEAN NOT NULL DEFAULT false,
	id_token_validity_seconds BIGINT NOT NULL DEFAULT 600,
	
	client_id VARCHAR(256),
	client_secret VARCHAR(2048),
	access_token_validity_seconds BIGINT,
	refresh_token_validity_seconds BIGINT,
	
	application_type VARCHAR(256),
	client_name VARCHAR(256),
	token_endpoint_auth_method VARCHAR(256),
	subject_type VARCHAR(256),
	
	logo_uri VARCHAR(2048),
	policy_uri VARCHAR(2048),
	client_uri VARCHAR(2048),
	tos_uri VARCHAR(2048),

	jwks_uri VARCHAR(2048),
	sector_identifier_uri VARCHAR(2048),
	
	request_object_signing_alg VARCHAR(256),
	
	user_info_signed_response_alg VARCHAR(256),
	user_info_encrypted_response_alg VARCHAR(256),
	user_info_encrypted_response_enc VARCHAR(256),
	
	id_token_signed_response_alg VARCHAR(256),
	id_token_encrypted_response_alg VARCHAR(256),
	id_token_encrypted_response_enc VARCHAR(256),
	
	default_max_age BIGINT,
	require_auth_time BOOLEAN,
	created_at TIMESTAMP,
	initiate_login_uri VARCHAR(2048),
	post_logout_redirect_uri VARCHAR(2048)
);

CREATE TABLE IF NOT EXISTS client_request_uri (
	owner_id BIGINT,
	request_uri VARCHAR(2000)
);

CREATE TABLE IF NOT EXISTS client_default_acr_value (
	owner_id BIGINT,
	default_acr_value VARCHAR(2000)
);

CREATE TABLE IF NOT EXISTS client_contact (
	owner_id BIGINT,
	contact VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS client_redirect_uri (
	owner_id BIGINT, 
	redirect_uri VARCHAR(2048) 
);

CREATE TABLE IF NOT EXISTS refresh_token (
	id BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY,
	token_value VARCHAR(4096),
	expiration TIMESTAMP,
	auth_holder_id BIGINT,
	client_id VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS client_resource (
	owner_id BIGINT, 
	resource_id VARCHAR(256) 
);

CREATE TABLE IF NOT EXISTS client_scope (
	owner_id BIGINT,
	scope VARCHAR(2048)
);

CREATE TABLE IF NOT EXISTS token_scope (
	owner_id BIGINT,
	scope VARCHAR(2048)
);

CREATE TABLE IF NOT EXISTS system_scope (
	id BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY,
	scope VARCHAR(256) NOT NULL,
	description VARCHAR(4096),
	icon VARCHAR(256),
	allow_dyn_reg BOOLEAN NOT NULL DEFAULT false,
	default_scope BOOLEAN NOT NULL DEFAULT false,
	structured BOOLEAN NOT NULL DEFAULT false,
	structured_param_description VARCHAR(256),
	UNIQUE (scope)
);

CREATE TABLE IF NOT EXISTS user_info (
	id BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY,
	sub VARCHAR(256),
	preferred_username VARCHAR(256),
	name VARCHAR(256),
	given_name VARCHAR(256),
	family_name VARCHAR(256),
	middle_name VARCHAR(256),
	nickname VARCHAR(256),
	profile VARCHAR(256),
	picture VARCHAR(256),
	website VARCHAR(256),
	email VARCHAR(256),
	email_verified BOOLEAN,
	gender VARCHAR(256),
	zone_info VARCHAR(256),
	locale VARCHAR(256),
	phone_number VARCHAR(256),
	address_id VARCHAR(256),
	updated_time VARCHAR(256),
	birthdate VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS whitelisted_site (
	id BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY,
	creator_user_id VARCHAR(256),
	client_id VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS whitelisted_site_scope (
	owner_id BIGINT,
	scope VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS trusted_registry  (
	uri VARCHAR(256)
);
