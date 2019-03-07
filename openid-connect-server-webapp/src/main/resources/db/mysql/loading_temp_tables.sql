-- 
-- Temporary tables used during the bootstrapping process to safely load users and clients.
-- These are not needed if you're not using the users.sql/clients.sql files to bootstrap the database.
-- 

CREATE GLOBAL TEMPORARY TABLE authorities_TEMP (
  username varchar(50) not null,
  authority varchar(50) not null,
  constraint ix_authority_TEMP unique (username,authority)
) ON COMMIT PRESERVE ROWS;
      
CREATE GLOBAL TEMPORARY TABLE users_TEMP (
  username varchar(50) not null primary key,
  password varchar(50) not null,
  enabled BOOLEAN not null
) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE user_info_TEMP (
	sub varchar(256) not null primary key,
	preferred_username varchar(256),
	name varchar(256),
	given_name varchar(256),
	family_name varchar(256),
	middle_name varchar(256),
	nickname varchar(256),
	profile varchar(256),
	picture varchar(256),
	website varchar(256),
	email varchar(256),
	email_verified BOOLEAN,
	gender varchar(256),
	zone_info varchar(256),
	locale varchar(256),
	phone_number varchar(256),
	address_id varchar(256),
	updated_time varchar(256),
	birthdate varchar(256)
) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE client_details_TEMP (
	client_description varchar(256),
	dynamically_registered BOOLEAN,
	id_token_validity_seconds BIGINT,
	
	client_id varchar(256),
	client_secret varchar(2048),
	access_token_validity_seconds BIGINT,
	refresh_token_validity_seconds BIGINT,
	allow_introspection BOOLEAN,
	
	client_name varchar(256)
) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE client_scope_TEMP (
	owner_id varchar(256),
	scope varchar(2048)
) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE client_redirect_uri_TEMP (
	owner_id varchar(256),
	redirect_uri varchar(2048) 
) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE client_grant_type_TEMP (
	owner_id varchar(256),
	grant_type varchar(2000)
) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE system_scope_TEMP (
	scope varchar(256),
	description varchar(4000),
	icon varchar(256),
	restricted BOOLEAN,
	default_scope BOOLEAN,
	structured BOOLEAN,
	structured_param_description varchar(256)
) ON COMMIT PRESERVE ROWS;
