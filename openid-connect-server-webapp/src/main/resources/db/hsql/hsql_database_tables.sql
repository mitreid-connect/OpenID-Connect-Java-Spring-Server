--
-- Tables for OIDC Server functionality, HSQL
--
CREATE TABLE IF NOT EXISTS host_info (
	uuid VARCHAR(64) PRIMARY KEY,
	owner_uuid VARCHAR(64),
	host VARCHAR(256),
	config text
	UNIQUE(host)
);

CREATE TABLE IF NOT EXISTS access_token (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	token_value VARCHAR(4096),
	expiration TIMESTAMP,
	token_type VARCHAR(256),
	refresh_token_uuid VARCHAR(64),
	client_uuid VARCHAR(64),
	auth_holder_uuid VARCHAR(64),
	approved_site_uuid VARCHAR(64)
	UNIQUE(token_value)
);

CREATE TABLE IF NOT EXISTS access_token_permissions (
	access_token_uuid BIGINT NOT NULL,
	permission_uuid BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS token_scope (
	access_token_uuid VARCHAR(64),
	scope VARCHAR(2048)
);

CREATE TABLE IF NOT EXISTS address (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	formatted VARCHAR(256),
	street_address VARCHAR(256),
	locality VARCHAR(256),
	region VARCHAR(256),
	postal_code VARCHAR(256),
	country VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS approved_site (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	user_id VARCHAR(256),
	client_id VARCHAR(256),
	creation_date TIMESTAMP,
	access_date TIMESTAMP,
	timeout_date TIMESTAMP,
	whitelisted_site_id BIGINT
);

CREATE TABLE IF NOT EXISTS approved_site_scope (
	approved_site_uuid VARCHAR(64),
	scope VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS authentication_holder (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	user_auth_uuid VARCHAR(64),
	approved BOOLEAN,
	redirect_uri VARCHAR(2048),
	client_id VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS authentication_holder_authority (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	auth_holder_uuid VARCHAR(64),
	authority VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS authentication_holder_resource_id (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	auth_holder_uuid VARCHAR(64),
	resource_id VARCHAR(2048)
);

CREATE TABLE IF NOT EXISTS authentication_holder_response_type (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	auth_holder_uuid VARCHAR(64),
	response_type VARCHAR(2048)
);

CREATE TABLE IF NOT EXISTS authentication_holder_extension (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	auth_holder_uuid VARCHAR(64),
	extension VARCHAR(2048),
	val VARCHAR(2048)
);

CREATE TABLE IF NOT EXISTS authentication_holder_scope (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	auth_holder_uuid VARCHAR(64),
	scope VARCHAR(2048)
);

CREATE TABLE IF NOT EXISTS authentication_holder_request_parameter (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	auth_holder_uuid VARCHAR(64),
	param VARCHAR(2048),
	val VARCHAR(2048)
);

CREATE TABLE IF NOT EXISTS saved_user_auth (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	name VARCHAR(1024),
	authenticated BOOLEAN,
	source_class VARCHAR(2048)
);

CREATE TABLE IF NOT EXISTS saved_user_auth_authority (
	user_auth_uuid VARCHAR(64),
	authority VARCHAR(256)
);


CREATE TABLE IF NOT EXISTS authorization_code (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	auth_holder_uuid VARCHAR(64),
	code VARCHAR(256),
	expiration TIMESTAMP
);


CREATE TABLE IF NOT EXISTS blacklisted_site (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	uri VARCHAR(2048)
);

CREATE TABLE IF NOT EXISTS client_details (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	client_description VARCHAR(1024),
	reuse_refresh_tokens BOOLEAN DEFAULT true NOT NULL,
	dynamically_registered BOOLEAN DEFAULT false NOT NULL,
	allow_introspection BOOLEAN DEFAULT false NOT NULL,
	id_token_validity_seconds BIGINT DEFAULT 600 NOT NULL,
	device_code_validity_seconds BIGINT,
	
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
	jwks VARCHAR(8192),
	sector_identifier_uri VARCHAR(2048),
	
	request_object_signing_alg VARCHAR(256),
	
	user_info_signed_response_alg VARCHAR(256),
	user_info_encrypted_response_alg VARCHAR(256),
	user_info_encrypted_response_enc VARCHAR(256),
	
	id_token_signed_response_alg VARCHAR(256),
	id_token_encrypted_response_alg VARCHAR(256),
	id_token_encrypted_response_enc VARCHAR(256),
	
	token_endpoint_auth_signing_alg VARCHAR(256),
	
	default_max_age BIGINT,
	require_auth_time BOOLEAN,
	created_at TIMESTAMP,
	initiate_login_uri VARCHAR(2048),
	clear_access_tokens_on_refresh BOOLEAN DEFAULT true NOT NULL,
	
	software_statement VARCHAR(4096),
	software_id VARCHAR(2048),
	software_version VARCHAR(2048),
	
	code_challenge_method VARCHAR(256),
	
	UNIQUE (client_id)
);

CREATE TABLE IF NOT EXISTS client_authority (
	client_uuid VARCHAR(64),
	authority VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS client_grant_type (
	client_uuid VARCHAR(64),
	grant_type VARCHAR(2000)
);

CREATE TABLE IF NOT EXISTS client_response_type (
	client_uuid VARCHAR(64),
	response_type VARCHAR(2000)
);


CREATE TABLE IF NOT EXISTS client_request_uri (
	client_uuid VARCHAR(64),
	request_uri VARCHAR(2000)
);

CREATE TABLE IF NOT EXISTS client_post_logout_redirect_uri (
	client_uuid VARCHAR(64),
	post_logout_redirect_uri VARCHAR(2000)
);

CREATE TABLE IF NOT EXISTS client_default_acr_value (
	client_uuid VARCHAR(64),
	default_acr_value VARCHAR(2000)
);

CREATE TABLE IF NOT EXISTS client_contact (
	client_uuid VARCHAR(64),
	contact VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS client_redirect_uri (
	client_uuid VARCHAR(64),
	redirect_uri VARCHAR(2048) 
);

CREATE TABLE IF NOT EXISTS client_claims_redirect_uri (
	client_uuid VARCHAR(64),
	redirect_uri VARCHAR(2048) 
);

CREATE TABLE IF NOT EXISTS refresh_token (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	client_uuid VARCHAR(64),
	auth_holder_uuid VARCHAR(64),
	token_value VARCHAR(4096),
	expiration TIMESTAMP
);

CREATE TABLE IF NOT EXISTS client_resource (
	client_uuid VARCHAR(64),
	resource_id VARCHAR(256) 
);

CREATE TABLE IF NOT EXISTS client_scope (
	client_uuid VARCHAR(64),
	scope VARCHAR(2048)
);

CREATE TABLE IF NOT EXISTS system_scope (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	scope VARCHAR(256) NOT NULL,
	description VARCHAR(4096),
	icon VARCHAR(256),
	restricted BOOLEAN DEFAULT false NOT NULL,
	default_scope BOOLEAN DEFAULT false NOT NULL,
	UNIQUE (scope)
);

CREATE TABLE IF NOT EXISTS user_info (
	user_uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),	
	sub VARCHAR(256),
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
	phone_number_verified BOOLEAN,
	address_id VARCHAR(256),
	updated_time VARCHAR(256),
	birthdate VARCHAR(256),
	src VARCHAR(4096)
);

CREATE TABLE IF NOT EXISTS whitelisted_site (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),	
	creator_user_uuid VARCHAR(64),
	client_uuid VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS whitelisted_site_scope (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),	
	whitelisted_site_uuid VARCHAR(64),
	scope VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS pairwise_identifier (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	identifier VARCHAR(256),
	sub VARCHAR(256),
	sector_identifier VARCHAR(2048)
);

CREATE TABLE IF NOT EXISTS resource_set (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	name VARCHAR(1024) NOT NULL,
	uri VARCHAR(1024),
	icon_uri VARCHAR(1024),
	rs_type VARCHAR(256),
	owner VARCHAR(256) NOT NULL,
	client_uuid VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS resource_set_scope (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	resource_set_uuid VARCHAR(64) NOT NULL,
	scope VARCHAR(256) NOT NULL
);

CREATE TABLE IF NOT EXISTS permission (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	resource_set_uuid VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS permission_ticket (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	ticket VARCHAR(256) NOT NULL,
	permission_uuid VARCHAR(64) NOT NULL,
	expiration TIMESTAMP
);

CREATE TABLE IF NOT EXISTS permission_scope (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	permission_uuid VARCHAR(64) NOT NULL,
	scope VARCHAR(256) NOT NULL
);

CREATE TABLE IF NOT EXISTS claim (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	name VARCHAR(256),
	friendly_name VARCHAR(1024),
	claim_type VARCHAR(1024),
	claim_value VARCHAR(1024)
);

CREATE TABLE IF NOT EXISTS claim_token_format (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	claim_uuid VARCHAR(64) NOT NULL,
	claim_token_format VARCHAR(1024)
);

CREATE TABLE IF NOT EXISTS claim_issuer (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	claim_uuid VARCHAR(64) NOT NULL,
	issuer VARCHAR(1024)
);

CREATE TABLE IF NOT EXISTS claim_to_permission_ticket (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	claim_uuid VARCHAR(64) NOT NULL,
	permission_ticket_uuid VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS policy (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	name VARCHAR(1024),
	resource_set_uuid VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS policy_scope (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	policy_uuid BIGINT NOT NULL,
	scope VARCHAR(256) NOT NULL
);

CREATE TABLE IF NOT EXISTS claim_to_policy (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	policy_uuid VARCHAR(64) NOT NULL,
	claim_uuid VARCHAR(64) NOT NULL
);



CREATE TABLE IF NOT EXISTS saved_registered_client (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	issuer VARCHAR(1024),
	registered_client VARCHAR(8192)
);

CREATE TABLE IF NOT EXISTS device_code (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	auth_holder_uuid VARCHAR(64,
	device_code VARCHAR(1024),
	user_code VARCHAR(1024),
	expiration TIMESTAMP,
	client_id VARCHAR(256),
	approved BOOLEAN	
);

CREATE TABLE IF NOT EXISTS device_code_scope (
	device_code_uuid VARCHAR(64) NOT NULL,
	scope VARCHAR(256) NOT NULL
);

CREATE TABLE IF NOT EXISTS device_code_request_parameter (
	device_code_uuid VARCHAR(64) NOT NULL,
	param VARCHAR(2048),
	val VARCHAR(2048)
);
