--
-- Tables for OIDC Server functionality, MySQL
--

CREATE TABLE IF NOT EXISTS access_token (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	token_value text,
	expiration TIMESTAMP NULL,
	token_type VARCHAR(255),
	refresh_token_id BIGINT,
	client_id BIGINT,
	auth_holder_id BIGINT,
	approved_site_id BIGINT
);

CREATE TABLE IF NOT EXISTS access_token_permissions (
	access_token_id BIGINT NOT NULL,
	permission_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS address (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	formatted VARCHAR(255),
	street_address VARCHAR(255),
	locality VARCHAR(255),
	region VARCHAR(255),
	postal_code VARCHAR(255),
	country VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS approved_site (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	user_id VARCHAR(255),
	client_id VARCHAR(255),
	creation_date TIMESTAMP NULL,
	access_date TIMESTAMP NULL,
	timeout_date TIMESTAMP NULL,
	whitelisted_site_id BIGINT
);

CREATE TABLE IF NOT EXISTS approved_site_scope (
	owner_id BIGINT,
	scope VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS authentication_holder (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	user_auth_id BIGINT,
	approved BOOLEAN,
	redirect_uri VARCHAR(512),
	client_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS authentication_holder_authority (
	owner_id BIGINT,
	authority VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS authentication_holder_resource_id (
	owner_id BIGINT,
	resource_id text
);

CREATE TABLE IF NOT EXISTS authentication_holder_response_type (
	owner_id BIGINT,
	response_type text
);

CREATE TABLE IF NOT EXISTS authentication_holder_extension (
	owner_id BIGINT,
	extension text,
	val text
);

CREATE TABLE IF NOT EXISTS authentication_holder_scope (
	owner_id BIGINT,
	scope text
);

CREATE TABLE IF NOT EXISTS authentication_holder_request_parameter (
	owner_id BIGINT,
	param text,
	val text
);

CREATE TABLE IF NOT EXISTS saved_user_auth (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	name VARCHAR(1024),
	authenticated BOOLEAN,
	source_class text
);

CREATE TABLE IF NOT EXISTS saved_user_auth_authority (
	owner_id BIGINT,
	authority VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS client_authority (
	owner_id BIGINT,
	authority VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS authorization_code (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	code VARCHAR(255),
	auth_holder_id BIGINT,
	expiration TIMESTAMP NULL
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
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	uri VARCHAR(1024)
);

CREATE TABLE IF NOT EXISTS client_details (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,

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

CREATE TABLE IF NOT EXISTS client_request_uri (
	owner_id BIGINT,
	request_uri VARCHAR(2000)
);

CREATE TABLE IF NOT EXISTS client_post_logout_redirect_uri (
	owner_id BIGINT,
	post_logout_redirect_uri VARCHAR(2000)
);

CREATE TABLE IF NOT EXISTS client_default_acr_value (
	owner_id BIGINT,
	default_acr_value VARCHAR(2000)
);

CREATE TABLE IF NOT EXISTS client_contact (
	owner_id BIGINT,
	contact VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS client_redirect_uri (
	owner_id BIGINT, 
	redirect_uri VARCHAR(512) 
);

CREATE TABLE IF NOT EXISTS client_claims_redirect_uri (
	owner_id BIGINT, 
	redirect_uri VARCHAR(512) 
);

CREATE TABLE IF NOT EXISTS refresh_token (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	token_value text,
	expiration TIMESTAMP NULL,
	auth_holder_id BIGINT,
	client_id BIGINT
);

CREATE TABLE IF NOT EXISTS client_resource (
	owner_id BIGINT, 
	resource_id VARCHAR(255) 
);

CREATE TABLE IF NOT EXISTS client_scope (
	owner_id BIGINT,
	scope text
);

CREATE TABLE IF NOT EXISTS token_scope (
	owner_id BIGINT,
	scope text
);

CREATE TABLE IF NOT EXISTS system_scope (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	scope VARCHAR(255) NOT NULL,
	description text,
	icon VARCHAR(255),
	restricted BOOLEAN DEFAULT false NOT NULL,
	default_scope BOOLEAN DEFAULT false NOT NULL,
	UNIQUE (scope)
);

CREATE TABLE IF NOT EXISTS user_info (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
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
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	creator_user_id VARCHAR(255),
	client_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS whitelisted_site_scope (
	owner_id BIGINT,
	scope VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS pairwise_identifier (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	identifier VARCHAR(255),
	sub VARCHAR(255),
	sector_identifier text
);

CREATE TABLE IF NOT EXISTS resource_set (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	name VARCHAR(1024) NOT NULL,
	uri VARCHAR(1024),
	icon_uri VARCHAR(512),
	rs_type VARCHAR(255),
	owner VARCHAR(255) NOT NULL,
	client_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS resource_set_scope (
	owner_id BIGINT NOT NULL,
	scope VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS permission_ticket (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	ticket VARCHAR(255) NOT NULL,
	permission_id BIGINT NOT NULL,
	expiration TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS permission (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	resource_set_id BIGINT
);

CREATE TABLE IF NOT EXISTS permission_scope (
	owner_id BIGINT NOT NULL,
	scope VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS claim (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	name VARCHAR(255),
	friendly_name VARCHAR(1024),
	claim_type VARCHAR(1024),
	claim_value VARCHAR(1024)
);

CREATE TABLE IF NOT EXISTS claim_to_policy (
	policy_id BIGINT NOT NULL,
	claim_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS claim_to_permission_ticket (
	permission_ticket_id BIGINT NOT NULL,
	claim_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS policy (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	name VARCHAR(1024),
	resource_set_id BIGINT
);

CREATE TABLE IF NOT EXISTS policy_scope (
	owner_id BIGINT NOT NULL,
	scope VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS claim_token_format (
	owner_id BIGINT NOT NULL,
	claim_token_format VARCHAR(1024)
);

CREATE TABLE IF NOT EXISTS claim_issuer (
	owner_id BIGINT NOT NULL,
	issuer VARCHAR(1024)
);

CREATE TABLE IF NOT EXISTS saved_registered_client (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	issuer VARCHAR(1024),
	registered_client text
);

CREATE TABLE IF NOT EXISTS device_code (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	device_code VARCHAR(1024),
	user_code VARCHAR(1024),
	expiration TIMESTAMP NULL,
	client_id VARCHAR(255),
	approved BOOLEAN,
	auth_holder_id BIGINT	
);

CREATE TABLE IF NOT EXISTS device_code_scope (
	owner_id BIGINT NOT NULL,
	scope VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS device_code_request_parameter (
	owner_id BIGINT,
	param text,
	val text
);
