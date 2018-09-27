--
-- Tables for OIDC Server functionality, MySQL
--

CREATE TABLE IF NOT EXISTS host_info (
	uuid VARCHAR(64) PRIMARY KEY,
	owner_uuid VARCHAR(64),
	host VARCHAR(256),
	config VARCHAR(8192),
	UNIQUE(host)
);

CREATE TABLE IF NOT EXISTS access_token (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	token_value text,
	expiration TIMESTAMP NULL,
	token_type VARCHAR(255),
	refresh_token_uuid VARCHAR(64),
	client_uuid VARCHAR(64),
	auth_holder_uuid VARCHAR(64),
	approved_site_uuid VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS access_token_permissions (
	access_token_uuid VARCHAR(64) NOT NULL,
	permission_uuid VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS token_scope (
	access_token_uuid VARCHAR(64),
	scope text
);

CREATE TABLE IF NOT EXISTS address (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),	
	formatted VARCHAR(255),
	street_address VARCHAR(255),
	locality VARCHAR(255),
	region VARCHAR(255),
	postal_code VARCHAR(255),
	country VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS approved_site (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),	
	user_id VARCHAR(255),
	client_id VARCHAR(255),
	creation_date TIMESTAMP NULL,
	access_date TIMESTAMP NULL,
	timeout_date TIMESTAMP NULL,
	whitelisted_site_uuid VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS approved_site_scope (
	approved_site_uuid VARCHAR(64),
	scope VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS authentication_holder (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),	
	user_auth_uuid VARCHAR(64),
	approved BOOLEAN,
	redirect_uri VARCHAR(512),
	client_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS authentication_holder_authority (
	auth_holder_uuid VARCHAR(64),
	authority VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS authentication_holder_resource_id (
	auth_holder_uuid VARCHAR(64),
	resource_id text
);

CREATE TABLE IF NOT EXISTS authentication_holder_response_type (
	auth_holder_uuid VARCHAR(64),
	response_type text
);

CREATE TABLE IF NOT EXISTS authentication_holder_extension (
	auth_holder_uuid VARCHAR(64),
	extension text,
	val text
);

CREATE TABLE IF NOT EXISTS authentication_holder_scope (
	auth_holder_uuid VARCHAR(64),
	scope text
);

CREATE TABLE IF NOT EXISTS authentication_holder_request_parameter (
	auth_holder_uuid VARCHAR(64),
	param text,
	val text
);

CREATE TABLE IF NOT EXISTS saved_user_auth (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),	
	name VARCHAR(1024),
	authenticated BOOLEAN,
	source_class text
);

CREATE TABLE IF NOT EXISTS saved_user_auth_authority (
	user_auth_uuid VARCHAR(64),
	authority VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS authorization_code (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),		
	code VARCHAR(255),
	auth_holder_uuid VARCHAR(64),
	expiration TIMESTAMP NULL
);


CREATE TABLE IF NOT EXISTS blacklisted_site (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),		
	uri VARCHAR(1024)
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
	
	client_id VARCHAR(255),
	client_secret text,
	access_token_validity_seconds BIGINT,
	refresh_token_validity_seconds BIGINT,
	
	application_type VARCHAR(255),
	client_name VARCHAR(255),
	token_endpoint_auth_method VARCHAR(255),
	subject_type VARCHAR(255),
	
	logo_uri VARCHAR(512),
	policy_uri VARCHAR(512),
	client_uri VARCHAR(512),
	tos_uri VARCHAR(512),

	jwks_uri VARCHAR(512),
	jwks text,
	sector_identifier_uri VARCHAR(512),
	
	request_object_signing_alg VARCHAR(255),
	
	user_info_signed_response_alg VARCHAR(255),
	user_info_encrypted_response_alg VARCHAR(255),
	user_info_encrypted_response_enc VARCHAR(255),
	
	id_token_signed_response_alg VARCHAR(255),
	id_token_encrypted_response_alg VARCHAR(255),
	id_token_encrypted_response_enc VARCHAR(255),
	
	token_endpoint_auth_signing_alg VARCHAR(255),
	
	default_max_age BIGINT,
	require_auth_time BOOLEAN,
	created_at TIMESTAMP NULL,
	initiate_login_uri VARCHAR(512),
	clear_access_tokens_on_refresh BOOLEAN DEFAULT true NOT NULL,
	
	software_statement text,
	software_id text,
	software_version text,
	
	code_challenge_method VARCHAR(255),
	
	UNIQUE (client_id)
);

CREATE TABLE IF NOT EXISTS client_authority (
	client_uuid VARCHAR(64),
	authority VARCHAR(255)
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
	contact VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS client_redirect_uri (
	client_uuid VARCHAR(64),
	redirect_uri VARCHAR(512) 
);

CREATE TABLE IF NOT EXISTS client_claims_redirect_uri (
	client_uuid VARCHAR(64), 
	redirect_uri VARCHAR(512) 
);

CREATE TABLE IF NOT EXISTS refresh_token (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),		
	token_value text,
	expiration TIMESTAMP NULL,
	auth_holder_uuid VARCHAR(64),
	client_uuid VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS client_resource (
	client_uuid VARCHAR(64),
	resource_id VARCHAR(255) 
);

CREATE TABLE IF NOT EXISTS client_scope (
	client_uuid VARCHAR(64),
	scope text
);

CREATE TABLE IF NOT EXISTS system_scope (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),		
	scope VARCHAR(255) NOT NULL,
	description text,
	icon VARCHAR(255),
	restricted BOOLEAN DEFAULT false NOT NULL,
	default_scope BOOLEAN DEFAULT false NOT NULL,
	UNIQUE (scope)
);

CREATE TABLE IF NOT EXISTS user_info (
	user_uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),		
	sub VARCHAR(255),
	preferred_username VARCHAR(255),
	name VARCHAR(255),
	given_name VARCHAR(255),
	family_name VARCHAR(255),
	middle_name VARCHAR(255),
	nickname VARCHAR(255),
	profile VARCHAR(255),
	picture VARCHAR(255),
	website VARCHAR(255),
	email VARCHAR(255),
	email_verified BOOLEAN,
	gender VARCHAR(255),
	zone_info VARCHAR(255),
	locale VARCHAR(255),
	phone_number VARCHAR(255),
	phone_number_verified BOOLEAN,
	address_id VARCHAR(255),
	updated_time VARCHAR(255),
	birthdate VARCHAR(255),
	src text
);

CREATE TABLE IF NOT EXISTS whitelisted_site (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),
	creator_user_uuid VARCHAR(64),
	client_uuid VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS whitelisted_site_scope (
	whitelisted_site_uuid VARCHAR(64),
	scope VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS pairwise_identifier (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),		
	identifier VARCHAR(255),
	sub VARCHAR(255),
	sector_identifier text
);

CREATE TABLE IF NOT EXISTS resource_set (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),		
	name VARCHAR(1024) NOT NULL,
	uri VARCHAR(1024),
	icon_uri VARCHAR(512),
	rs_type VARCHAR(255),
	owner VARCHAR(255) NOT NULL,
	client_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS resource_set_scope (
	resource_set_uuid VARCHAR(64) NOT NULL,
	scope VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS permission_ticket (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),		
	ticket VARCHAR(255) NOT NULL,
	permission_uuid VARCHAR(64) NOT NULL,
	expiration TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS permission (
	uuid VARCHAR(64) PRIMARY KEY,
	resource_set_uuid VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS permission_scope (
	permission_uuid VARCHAR(64) NOT NULL,
	scope VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS claim (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),		
	name VARCHAR(255),
	friendly_name VARCHAR(1024),
	claim_type VARCHAR(1024),
	claim_value VARCHAR(1024)
);

CREATE TABLE IF NOT EXISTS claim_to_policy (
	policy_uuid VARCHAR(64) NOT NULL,
	claim_uuid VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS claim_token_format (
	claim_uuid VARCHAR(64) NOT NULL,
	claim_token_format VARCHAR(1024)
);

CREATE TABLE IF NOT EXISTS claim_issuer (
	claim_uuid VARCHAR(64) NOT NULL,
	issuer VARCHAR(1024)
);

CREATE TABLE IF NOT EXISTS claim_to_permission_ticket (
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
	policy_uuid VARCHAR(64) NOT NULL,
	scope VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS saved_registered_client (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),			
	issuer VARCHAR(1024),
	registered_client text
);

CREATE TABLE IF NOT EXISTS device_code (
	uuid VARCHAR(64) PRIMARY KEY,
	host_uuid VARCHAR(64),			
	device_code VARCHAR(1024),
	user_code VARCHAR(1024),
	expiration TIMESTAMP NULL,
	client_id VARCHAR(255),
	approved BOOLEAN,
	auth_holder_uuid VARCHAR(64)	
);

CREATE TABLE IF NOT EXISTS device_code_scope (
	device_code_uuid VARCHAR(64) NOT NULL,
	scope VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS device_code_request_parameter (
	device_code_uuid VARCHAR(64) NOT NULL,
	param text,
	val text
);
