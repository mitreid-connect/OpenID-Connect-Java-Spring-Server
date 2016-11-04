--
-- Temporary tables used during the bootstrapping process to safely load users and clients.
-- These are not needed if you're not using the users.sql/clients.sql files to bootstrap the database.
--

CREATE GLOBAL TEMPORARY TABLE authorities_TEMP (
  username varchar2(50) not null,
  authority varchar2(50) not null,
  constraint ix_authority_TEMP unique (username,authority)
) ON COMMIT PRESERVE ROWS;
      
CREATE GLOBAL TEMPORARY TABLE users_TEMP (
  username VARCHAR2(50) not null primary key,
  password VARCHAR2(50) not null,
  enabled NUMBER(1) not null
) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE user_info_TEMP (
	sub VARCHAR2(256) not null primary key,
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
	address_id VARCHAR2(256),
	updated_time VARCHAR2(256),
	birthdate VARCHAR2(256)
) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE client_details_TEMP (
	client_description VARCHAR2(256),
	dynamically_registered NUMBER(1),
	id_token_validity_seconds NUMBER(19),
	
	client_id VARCHAR2(256),
	client_secret VARCHAR2(2048),
	access_token_validity_seconds NUMBER(19),
	refresh_token_validity_seconds NUMBER(19),
	allow_introspection NUMBER(1),
	
	client_name VARCHAR2(256)
) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE client_scope_TEMP (
	owner_id VARCHAR2(256),
	scope VARCHAR2(2048)
) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE client_redirect_uri_TEMP (
	owner_id VARCHAR2(256),
	redirect_uri VARCHAR2(2048) 
) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE client_grant_type_TEMP (
	owner_id VARCHAR2(256),
	grant_type VARCHAR2(2000)
) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE system_scope_TEMP (
	scope VARCHAR2(256),
	description VARCHAR2(4000),
	icon VARCHAR2(256),
	restricted NUMBER(1),
	default_scope NUMBER(1),
	structured NUMBER(1),
	structured_param_description VARCHAR2(256)
) ON COMMIT PRESERVE ROWS;
