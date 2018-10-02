--
-- Tables for OIDC Server functionality, Oracle
--

CREATE TABLE host_info (
	uuid VARCHAR2(64) PRIMARY KEY,
	owner_uuid VARCHAR2(64),
	host VARCHAR2(256),
	config CLOB,
	UNIQUE(host)
);

CREATE TABLE access_token (
  uuid VARCHAR2(64) NOT NULL PRIMARY KEY,
  host_uuid VARCHAR2(64),
  token_value CLOB,
  expiration TIMESTAMP,
  token_type VARCHAR2(256),
  refresh_token_uuid VARCHAR2(64),
  client_uuid VARCHAR2(64),
  auth_holder_uuid VARCHAR2(64),
  approved_site_uuid VARCHAR2(64)
);

CREATE TABLE token_scope (
  access_token_uuid VARCHAR2(64),
  scope VARCHAR2(2048)
);

CREATE TABLE access_token_permissions (
  access_token_uuid VARCHAR2(64) NOT NULL,
  permission_uuid VARCHAR2(64) NOT NULL
);

CREATE TABLE address (
  uuid VARCHAR2(64) NOT NULL PRIMARY KEY,
  host_uuid VARCHAR2(64),
  formatted VARCHAR2(256),
  street_address VARCHAR2(256),
  locality VARCHAR2(256),
  region VARCHAR2(256),
  postal_code VARCHAR2(256),
  country VARCHAR2(256)
);

CREATE TABLE approved_site (
  uuid VARCHAR2(64) NOT NULL PRIMARY KEY,
  host_uuid VARCHAR2(64),
  user_id VARCHAR2(256),
  client_id VARCHAR2(256),
  creation_date TIMESTAMP,
  access_date TIMESTAMP,
  timeout_date TIMESTAMP,
  whitelisted_site_uuid VARCHAR2(64)
);

CREATE TABLE approved_site_scope (
  approved_site_uuid VARCHAR2(64),
  scope VARCHAR2(256)
);

CREATE TABLE authentication_holder (
  uuid VARCHAR2(64) NOT NULL PRIMARY KEY,
  host_uuid VARCHAR2(64),
  user_auth_uuid VARCHAR2(64),
  approved NUMBER(1),
  redirect_uri VARCHAR2(2048),
  client_id VARCHAR2(256),

  CONSTRAINT approved_check CHECK (approved in (1,0))
);

CREATE TABLE auth_holder_authority (
  auth_holder_uuid VARCHAR2(64),
  authority VARCHAR2(256)
);

CREATE TABLE auth_holder_resource_id (
  auth_holder_uuid VARCHAR2(64),
  resource_id VARCHAR2(2048)
);

CREATE TABLE auth_holder_resp_type (
  auth_holder_uuid VARCHAR2(64),
  response_type VARCHAR2(2048)
);

CREATE TABLE auth_holder_extension (
  auth_holder_uuid VARCHAR2(64),
  extension VARCHAR2(2048),
  val VARCHAR2(2048)
);

CREATE TABLE auth_holder_scope (
  auth_holder_uuid VARCHAR2(64),
  scope VARCHAR2(2048)
);

CREATE TABLE auth_holder_request_parameter (
  auth_holder_uuid VARCHAR2(64),
  param VARCHAR2(2048),
  val VARCHAR2(2048)
);

CREATE TABLE saved_user_auth (
  uuid VARCHAR2(64) NOT NULL PRIMARY KEY,
  host_uuid VARCHAR2(64),
  name VARCHAR2(1024),
  authenticated NUMBER(1),
  source_class VARCHAR2(2048),

  CONSTRAINT authenticated_check CHECK (authenticated in (1,0))
);

CREATE TABLE saved_user_auth_authority (
  user_auth_uuid VARCHAR2(64),
  authority VARCHAR2(256)
);


CREATE TABLE authorization_code (
  uuid VARCHAR2(64) NOT NULL PRIMARY KEY,
  host_uuid VARCHAR2(64),
  code VARCHAR2(256),
  auth_holder_uuid VARCHAR2(64),
  expiration TIMESTAMP
);


CREATE TABLE blacklisted_site (
  uuid VARCHAR2(64) NOT NULL PRIMARY KEY,
  host_uuid VARCHAR2(64),
  uri VARCHAR2(2048)
);

CREATE TABLE client_details (
  uuid VARCHAR2(64) NOT NULL PRIMARY KEY,

  host_uuid VARCHAR2(64),
  client_description VARCHAR2(1024),
  reuse_refresh_tokens NUMBER(1) DEFAULT 1 NOT NULL,
  dynamically_registered NUMBER(1) DEFAULT 0 NOT NULL,
  allow_introspection NUMBER(1) DEFAULT 0 NOT NULL,
  id_token_validity_seconds NUMBER(19) DEFAULT 600 NOT NULL,

  client_id VARCHAR2(256),
  client_secret VARCHAR2(2048),
  access_token_validity_seconds NUMBER(19),
  refresh_token_validity_seconds NUMBER(19),
  device_code_validity_seconds NUMBER(19),

  application_type VARCHAR2(256),
  client_name VARCHAR2(256),
  token_endpoint_auth_method VARCHAR2(256),
  subject_type VARCHAR2(256),

  logo_uri VARCHAR2(2048),
  policy_uri VARCHAR2(2048),
  client_uri VARCHAR2(2048),
  tos_uri VARCHAR2(2048),

  jwks_uri VARCHAR2(2048),
  jwks CLOB,
  sector_identifier_uri VARCHAR2(2048),

  request_object_signing_alg VARCHAR2(256),

  user_info_signed_resp_alg VARCHAR2(256),
  user_info_encrypted_resp_alg VARCHAR2(256),
  user_info_encrypted_resp_enc VARCHAR2(256),

  id_token_signed_resp_alg VARCHAR2(256),
  id_token_encrypted_resp_alg VARCHAR2(256),
  id_token_encrypted_resp_enc VARCHAR2(256),

  token_endpoint_auth_sign_alg VARCHAR2(256),

  default_max_age NUMBER(19),
  require_auth_time NUMBER(1),
  created_at TIMESTAMP,
  initiate_login_uri VARCHAR2(2048),
  clear_access_tokens_on_refresh NUMBER(1) DEFAULT 1 NOT NULL,
  
  software_id VARCHAR2(256),
  software_statement VARCHAR2(256),
  software_version VARCHAR2(256),
	
  code_challenge_method VARCHAR2(256),

  CONSTRAINT client_details_unique UNIQUE (client_id),
  CONSTRAINT reuse_refresh_tokens_check CHECK (reuse_refresh_tokens in (1,0)),
  CONSTRAINT dynamically_registered_check CHECK (dynamically_registered in (1,0)),
  CONSTRAINT allow_introspection_check CHECK (allow_introspection in (1,0)),
  CONSTRAINT require_auth_time_check CHECK (require_auth_time in (1,0)),
  CONSTRAINT clear_acc_tok_on_refresh_check CHECK (clear_access_tokens_on_refresh in (1,0))
);

CREATE TABLE client_authority (
  client_uuid VARCHAR2(64),
  authority VARCHAR2(256)
);

CREATE TABLE client_grant_type (
  client_uuid VARCHAR2(64),
  grant_type VARCHAR2(2000)
);

CREATE TABLE client_resp_type (
  client_uuid VARCHAR2(64),
  response_type VARCHAR2(2000)
);

CREATE TABLE client_request_uri (
  client_uuid VARCHAR2(64),
  request_uri VARCHAR2(2000)
);

CREATE TABLE client_post_logout_redir_uri (
  client_uuid VARCHAR2(64),
  post_logout_redirect_uri VARCHAR2(2000)
);

CREATE TABLE client_default_acr_value (
  client_uuid VARCHAR2(64),
  default_acr_value VARCHAR2(2000)
);

CREATE TABLE client_contact (
  client_uuid VARCHAR2(64),
  contact VARCHAR2(256)
);

CREATE TABLE client_redirect_uri (
  client_uuid VARCHAR2(64),
  redirect_uri VARCHAR2(2048)
);

CREATE TABLE client_claims_redirect_uri (
  client_uuid VARCHAR2(64),
  redirect_uri VARCHAR2(2048)
);

CREATE TABLE refresh_token (
  uuid VARCHAR2(64) NOT NULL PRIMARY KEY,
  host_uuid VARCHAR2(64),
  token_value CLOB,
  expiration TIMESTAMP,
  auth_holder_uuid VARCHAR2(64),
  client_uuid VARCHAR2(64)
);

CREATE TABLE client_resource (
  client_uuid VARCHAR2(64),
  resource_id VARCHAR2(256)
);

CREATE TABLE client_scope (
  client_uuid VARCHAR2(64),
  scope VARCHAR2(2048)
);

CREATE TABLE system_scope (
  uuid VARCHAR2(64) NOT NULL PRIMARY KEY,
  host_uuid VARCHAR2(64),
  scope VARCHAR2(256) NOT NULL,
  description CLOB,
  icon VARCHAR2(256),
  restricted NUMBER(1) DEFAULT 0 NOT NULL,
  default_scope NUMBER(1) DEFAULT 0 NOT NULL,

  CONSTRAINT system_scope_unique UNIQUE (scope),
  CONSTRAINT default_scope_check CHECK (default_scope in (1,0)),
  CONSTRAINT restricted_check CHECK (restricted in (1,0))
);

CREATE TABLE user_info (
  user_uuid VARCHAR2(64) NOT NULL PRIMARY KEY,
  host_uuid VARCHAR2(64),
  sub VARCHAR2(256),
  preferred_username VARCHAR2(256),
  name VARCHAR2(256),
  given_name VARCHAR2(256),
  family_name VARCHAR2(256),
  middle_name VARCHAR2(256),
  nickname VARCHAR2(256),
  profile VARCHAR2(256),
  picture VARCHAR2(256),
  website VARCHAR2(256),
  email VARCHAR2(256),
  email_verified NUMBER(1),
  gender VARCHAR2(256),
  zone_info VARCHAR2(256),
  locale VARCHAR2(256),
  phone_number VARCHAR2(256),
  phone_number_verified NUMBER(1),
  address_id VARCHAR2(256),
  updated_time VARCHAR2(256),
  birthdate VARCHAR2(256),
  src CLOB,

  CONSTRAINT email_verified_check CHECK (email_verified in (1,0)),
  CONSTRAINT phone_number_verified_check CHECK (phone_number_verified in (1,0))
);

CREATE TABLE whitelisted_site (
  uuid VARCHAR(64) PRIMARY KEY,
  host_uuid VARCHAR(64),
  creator_user_uuid VARCHAR(64),
  client_uuid VARCHAR(64)
);

CREATE TABLE whitelisted_site_scope (
  whitelisted_site_uuid VARCHAR2(64),
  scope VARCHAR2(256)
);

CREATE TABLE pairwise_identifier (
  uuid VARCHAR2(64) NOT NULL PRIMARY KEY,
  host_uuid VARCHAR2(64),
  identifier VARCHAR2(256),
  sub VARCHAR2(256),
  sector_identifier VARCHAR2(2048)
);

CREATE TABLE resource_set (
  uuid VARCHAR2(64) NOT NULL PRIMARY KEY,
  host_uuid VARCHAR2(64),
  name VARCHAR2(1024) NOT NULL,
  uri VARCHAR2(1024),
  icon_uri VARCHAR2(1024),
  rs_type VARCHAR2(256),
  owner VARCHAR2(256) NOT NULL,
  client_id VARCHAR2(256)
);

CREATE TABLE resource_set_scope (
  resource_set_uuid VARCHAR2(64) NOT NULL,
  scope VARCHAR2(256) NOT NULL
);

CREATE TABLE permission_ticket (
  uuid VARCHAR2(64) NOT NULL PRIMARY KEY,
  host_uuid VARCHAR2(64),
  ticket VARCHAR2(256) NOT NULL,
  permission_uuid VARCHAR2(64) NOT NULL,
  expiration TIMESTAMP
);

CREATE TABLE permission (
  uuid VARCHAR2(64) NOT NULL PRIMARY KEY,
  resource_set_uuid VARCHAR2(64)
);

CREATE TABLE permission_scope (
  permission_uuid VARCHAR2(64) NOT NULL,
  scope VARCHAR2(256) NOT NULL
);

CREATE TABLE claim (
  uuid VARCHAR2(64) NOT NULL PRIMARY KEY,
  host_uuid VARCHAR2(64),
  name VARCHAR2(256),
  friendly_name VARCHAR2(1024),
  claim_type VARCHAR2(1024),
  claim_value VARCHAR2(1024)
);

CREATE TABLE claim_token_format (
  claim_uuid VARCHAR2(64) NOT NULL,
  claim_token_format VARCHAR2(1024) NOT NULL
);

CREATE TABLE claim_issuer (
  claim_uuid VARCHAR2(64) NOT NULL,
  issuer VARCHAR2(1024) NOT NULL
);

CREATE TABLE claim_to_policy (
  policy_uuid VARCHAR2(64) NOT NULL,
  claim_uuid VARCHAR2(64) NOT NULL
);

CREATE TABLE claim_to_permission_ticket (
  permission_ticket_uuid VARCHAR2(64) NOT NULL,
  claim_uuid VARCHAR2(64) NOT NULL
);

CREATE TABLE policy (
  uuid VARCHAR2(64) NOT NULL PRIMARY KEY,
  host_uuid VARCHAR2(64),
  name VARCHAR2(1024),
  resource_set_uuid VARCHAR2(64)
);

CREATE TABLE policy_scope (
  policy_uuid VARCHAR2(64) NOT NULL,
  scope VARCHAR2(256) NOT NULL
);

CREATE TABLE saved_registered_client (
  uuid VARCHAR2(64) NOT NULL PRIMARY KEY,
  host_uuid VARCHAR2(64),
  issuer VARCHAR2(1024),
  registered_client CLOB
);

CREATE TABLE device_code (
  uuid VARCHAR2(64) NOT NULL PRIMARY KEY,
  host_uuid VARCHAR2(64),
  device_code VARCHAR2(1024),
  user_code VARCHAR2(1024),
  expiration TIMESTAMP,
  client_id VARCHAR2(256),
  approved NUMBER(1,0),
  auth_holder_uuid VARCHAR2(64)	
);

CREATE TABLE device_code_scope (
  device_code_uuid VARCHAR2(64) NOT NULL,
  scope VARCHAR2(256) NOT NULL
);

CREATE TABLE device_code_request_parameter (
  device_code_uuid VARCHAR2(64),
  param VARCHAR2(256),
  val CLOB
);
