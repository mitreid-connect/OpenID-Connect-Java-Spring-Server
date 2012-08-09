CREATE TABLE client_details (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	client_description VARCHAR(256),
	allow_refresh TINYINT,
	allow_multiple_access_tokens TINYINT,
	reuse_refresh_tokens TINYINT,
	
	client_id VARCHAR(256),
	client_secret VARCHAR(2000),
	access_token_validity_seconds BIGINT,
	refresh_token_validity_seconds BIGINT,
	
	application_type VARCHAR(256),
	application_name VARCHAR(256),
	token_endpoint_auth_type VARCHAR(256),
	user_id_type VARCHAR(256),
	
	logo_url VARCHAR(256),
	policy_url VARCHAR(256),
	jwk_url VARCHAR(256),
	jwk_encryption_url VARCHAR(256),
	x509_url VARCHAR(256),
	x509_encryption_url VARCHAR(256),
	sector_identifier_url VARCHAR(256),
	
	requre_signed_request_object VARCHAR(256),
	
	user_info_signed_response_alg VARCHAR(256),
	user_info_encrypted_response_alg VARCHAR(256),
	user_info_encrypted_response_enc VARCHAR(256),
	user_info_encrypted_response_int VARCHAR(256),
	
	id_token_signed_response_alg VARCHAR(256),
	id_token_encrypted_response_alg VARCHAR(256),
	id_token_encrypted_response_enc VARCHAR(256),
	id_token_encrypted_response_int VARCHAR(256),
	
	default_max_age BIGINT,
	require_auth_time TINYINT,
	default_acr VARCHAR(256)
);