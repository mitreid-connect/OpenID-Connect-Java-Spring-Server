--
-- Tables for OIDC Server functionality, Oracle
--

CREATE TABLE access_token (
  id NUMBER(19) NOT NULL PRIMARY KEY,
  token_value VARCHAR2(4000),
  expiration TIMESTAMP,
  token_type VARCHAR2(256),
  refresh_token_id NUMBER(19),
  client_id NUMBER(19),
  auth_holder_id NUMBER(19),
  approved_site_id NUMBER(19)
);
CREATE SEQUENCE access_token_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE TABLE access_token_permissions (
  access_token_id NUMBER(19) NOT NULL,
  permission_id NUMBER(19) NOT NULL
);

CREATE TABLE address (
  id NUMBER(19) NOT NULL PRIMARY KEY,
  formatted VARCHAR2(256),
  street_address VARCHAR2(256),
  locality VARCHAR2(256),
  region VARCHAR2(256),
  postal_code VARCHAR2(256),
  country VARCHAR2(256)
);
CREATE SEQUENCE address_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE TABLE approved_site (
  id NUMBER(19) NOT NULL PRIMARY KEY,
  user_id VARCHAR2(256),
  client_id VARCHAR2(256),
  creation_date TIMESTAMP,
  access_date TIMESTAMP,
  timeout_date TIMESTAMP,
  whitelisted_site_id NUMBER(19)
);
CREATE SEQUENCE approved_site_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE TABLE approved_site_scope (
  owner_id NUMBER(19),
  scope VARCHAR2(256)
);

CREATE TABLE authentication_holder (
  id NUMBER(19) NOT NULL PRIMARY KEY,
  user_auth_id NUMBER(19),
  approved NUMBER(1),
  redirect_uri VARCHAR2(2048),
  client_id VARCHAR2(256),

  CONSTRAINT approved_check CHECK (approved in (1,0))
);
CREATE SEQUENCE authentication_holder_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE TABLE auth_holder_authority (
  owner_id NUMBER(19),
  authority VARCHAR2(256)
);

CREATE TABLE auth_holder_resource_id (
  owner_id NUMBER(19),
  resource_id VARCHAR2(2048)
);

CREATE TABLE auth_holder_response_type (
  owner_id NUMBER(19),
  response_type VARCHAR2(2048)
);

CREATE TABLE auth_holder_extension (
  owner_id NUMBER(19),
  extension VARCHAR2(2048),
  val VARCHAR2(2048)
);

CREATE TABLE authentication_holder_scope (
  owner_id NUMBER(19),
  scope VARCHAR2(2048)
);

CREATE TABLE auth_holder_request_parameter (
  owner_id NUMBER(19),
  param VARCHAR2(2048),
  val VARCHAR2(2048)
);

CREATE TABLE saved_user_auth (
  id NUMBER(19) NOT NULL PRIMARY KEY,
  name VARCHAR2(1024),
  authenticated NUMBER(1),
  source_class VARCHAR2(2048),

  CONSTRAINT authenticated_check CHECK (authenticated in (1,0))
);
CREATE SEQUENCE saved_user_auth_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE TABLE saved_user_auth_authority (
  owner_id NUMBER(19),
  authority VARCHAR2(256)
);

CREATE TABLE client_authority (
  owner_id NUMBER(19),
  authority VARCHAR2(256)
);

CREATE TABLE authorization_code (
  id NUMBER(19) NOT NULL PRIMARY KEY,
  code VARCHAR2(256),
  auth_holder_id NUMBER(19),
  expiration TIMESTAMP
);
CREATE SEQUENCE authorization_code_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE TABLE client_grant_type (
  owner_id NUMBER(19),
  grant_type VARCHAR2(2000)
);

CREATE TABLE client_response_type (
  owner_id NUMBER(19),
  response_type VARCHAR2(2000)
);

CREATE TABLE blacklisted_site (
  id NUMBER(19) NOT NULL PRIMARY KEY,
  uri VARCHAR2(2048)
);
CREATE SEQUENCE blacklisted_site_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE TABLE client_details (
  id NUMBER(19) NOT NULL PRIMARY KEY,

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

  policy_uri VARCHAR2(2048),
  client_uri VARCHAR2(2048),
  tos_uri VARCHAR2(2048),

  jwks_uri VARCHAR2(2048),
  jwks CLOB,
  sector_identifier_uri VARCHAR2(2048),

  request_object_signing_alg VARCHAR2(256),

  user_info_signed_response_alg VARCHAR2(256),
  user_info_encrypted_resp_alg VARCHAR2(256),
  user_info_encrypted_resp_enc VARCHAR2(256),

  id_token_signed_response_alg VARCHAR2(256),
  id_token_encrypted_resp_alg VARCHAR2(256),
  id_token_encrypted_resp_enc VARCHAR2(256),

  token_endpoint_auth_sign_alg VARCHAR2(256),

  default_max_age NUMBER(19),
  require_auth_time NUMBER(1),
  created_at TIMESTAMP,
  initiate_login_uri VARCHAR2(2048),
  clear_access_tokens_on_refresh NUMBER(1) DEFAULT 1 NOT NULL,
  
  software_statement VARCHAR(4096),
  software_id VARCHAR(2048),
  software_statement VARCHAR2(4000),
	
  code_challenge_method VARCHAR2(256),

  CONSTRAINT client_details_unique UNIQUE (client_id),
  CONSTRAINT reuse_refresh_tokens_check CHECK (reuse_refresh_tokens in (1,0)),
  CONSTRAINT dynamically_registered_check CHECK (dynamically_registered in (1,0)),
  CONSTRAINT allow_introspection_check CHECK (allow_introspection in (1,0)),
  CONSTRAINT require_auth_time_check CHECK (require_auth_time in (1,0)),
  CONSTRAINT clear_acc_tok_on_refresh_check CHECK (clear_access_tokens_on_refresh in (1,0))
);
CREATE SEQUENCE client_details_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE TABLE client_request_uri (
  owner_id NUMBER(19),
  request_uri VARCHAR2(2000)
);

CREATE TABLE client_post_logout_redir_uri (
  owner_id NUMBER(19),
  post_logout_redirect_uri VARCHAR2(2000)
);

CREATE TABLE client_default_acr_value (
  owner_id NUMBER(19),
  default_acr_value VARCHAR2(2000)
);

CREATE TABLE client_contact (
  owner_id NUMBER(19),
  contact VARCHAR2(256)
);

CREATE TABLE client_redirect_uri (
  owner_id NUMBER(19),
  redirect_uri VARCHAR2(2048)
);

CREATE TABLE client_claims_redirect_uri (
  owner_id NUMBER(19),
  redirect_uri VARCHAR2(2048)
);

CREATE TABLE refresh_token (
  id NUMBER(19) NOT NULL PRIMARY KEY,
  token_value VARCHAR2(4000),
  expiration TIMESTAMP,
  auth_holder_id NUMBER(19),
  client_id NUMBER(19)
);
CREATE SEQUENCE refresh_token_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE TABLE client_resource (
  owner_id NUMBER(19),
  resource_id VARCHAR2(256)
);

CREATE TABLE client_scope (
  owner_id NUMBER(19),
  scope VARCHAR2(2048)
);

CREATE TABLE token_scope (
  owner_id NUMBER(19),
  scope VARCHAR2(2048)
);

CREATE TABLE system_scope (
  id NUMBER(19) NOT NULL PRIMARY KEY,
  scope VARCHAR2(256) NOT NULL,
  description VARCHAR2(4000),
  icon VARCHAR2(256),
  restricted NUMBER(1) DEFAULT 0 NOT NULL,
  default_scope NUMBER(1) DEFAULT 0 NOT NULL

  CONSTRAINT system_scope_unique UNIQUE (scope),
  CONSTRAINT default_scope_check CHECK (default_scope in (1,0)),
  CONSTRAINT restricted_check CHECK (restricted in (1,0))
);
CREATE SEQUENCE system_scope_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE TABLE user_info (
  id NUMBER(19) NOT NULL PRIMARY KEY,
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
  src VARCHAR2(4000),

  CONSTRAINT email_verified_check CHECK (email_verified in (1,0)),
  CONSTRAINT phone_number_verified_check CHECK (phone_number_verified in (1,0))
);
CREATE SEQUENCE user_info_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE TABLE whitelisted_site (
  id NUMBER(19) NOT NULL PRIMARY KEY,
  creator_user_id VARCHAR2(256),
  client_id VARCHAR2(256)
);
CREATE SEQUENCE whitelisted_site_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE TABLE whitelisted_site_scope (
  owner_id NUMBER(19),
  scope VARCHAR2(256)
);

CREATE TABLE pairwise_identifier (
  id NUMBER(19) NOT NULL PRIMARY KEY,
  identifier VARCHAR2(256),
  sub VARCHAR2(256),
  sector_identifier VARCHAR2(2048)
);
CREATE SEQUENCE pairwise_identifier_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE TABLE resource_set (
  id NUMBER(19) NOT NULL PRIMARY KEY,
  name VARCHAR2(1024) NOT NULL,
  uri VARCHAR2(1024),
  icon_uri VARCHAR2(1024),
  rs_type VARCHAR2(256),
  owner VARCHAR2(256) NOT NULL,
  client_id VARCHAR2(256)
);
CREATE SEQUENCE resource_set_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE TABLE resource_set_scope (
  owner_id NUMBER(19) NOT NULL,
  scope VARCHAR2(256) NOT NULL
);

CREATE TABLE permission_ticket (
  id NUMBER(19) NOT NULL PRIMARY KEY,
  ticket VARCHAR2(256) NOT NULL,
  permission_id NUMBER(19) NOT NULL,
  expiration TIMESTAMP
);
CREATE SEQUENCE permission_ticket_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE TABLE permission (
  id NUMBER(19) NOT NULL PRIMARY KEY,
  resource_set_id NUMBER(19)
);
CREATE SEQUENCE permission_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE TABLE permission_scope (
  owner_id NUMBER(19) NOT NULL,
  scope VARCHAR2(256) NOT NULL
);

CREATE TABLE claim (
  id NUMBER(19) NOT NULL PRIMARY KEY,
  name VARCHAR2(256),
  friendly_name VARCHAR2(1024),
  claim_type VARCHAR2(1024),
  claim_value VARCHAR2(1024)
);
CREATE SEQUENCE claim_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE TABLE claim_to_policy (
  policy_id NUMBER(19) NOT NULL,
  claim_id NUMBER(19) NOT NULL
);

CREATE TABLE claim_to_permission_ticket (
  permission_ticket_id NUMBER(19) NOT NULL,
  claim_id NUMBER(19) NOT NULL
);

CREATE TABLE policy (
  id NUMBER(19) NOT NULL PRIMARY KEY,
  name VARCHAR2(1024),
  resource_set_id NUMBER(19)
);
CREATE SEQUENCE policy_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE TABLE policy_scope (
  owner_id NUMBER(19) NOT NULL,
  scope VARCHAR2(256) NOT NULL
);

CREATE TABLE claim_token_format (
  owner_id NUMBER(19) NOT NULL,
  claim_token_format VARCHAR2(1024) NOT NULL
);

CREATE TABLE claim_issuer (
  owner_id NUMBER(19) NOT NULL,
  issuer VARCHAR2(1024) NOT NULL
);

CREATE TABLE saved_registered_client (
  id NUMBER(19) NOT NULL PRIMARY KEY,
  issuer VARCHAR2(1024),
  registered_client CLOB
);
CREATE SEQUENCE saved_registered_client_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE TABLE IF NOT EXISTS device_code (
	id NUMBER(19) NOT NULL PRIMARY KEY,
	device_code VARCHAR2(1024),
	user_code VARCHAR2(1024),
	expiration TIMESTAMP,
	client_id VARCHAR2(256),
	approved BOOLEAN,
	auth_holder_id NUMBER(19)	
);

CREATE TABLE IF NOT EXISTS device_code_scope (
	owner_id NUMBER(19) NOT NULL,
	scope VARCHAR2(256) NOT NULL
);

CREATE TABLE IF NOT EXISTS device_code_request_parameter (
	owner_id NUMBER(19),
	param VARCHAR2(2048),
	val VARCHAR2(2048)
);
