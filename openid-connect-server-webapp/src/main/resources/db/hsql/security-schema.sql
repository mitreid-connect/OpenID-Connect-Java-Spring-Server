-- 
-- Tables for Spring Security's user details service
--
  
CREATE TABLE IF NOT EXISTS user (
	  uuid VARCHAR(64) PRIMARY KEY,
	  host_uuid VARCHAR(64),
      username VARCHAR(255) not null,
      password VARCHAR(255) not null,
      enabled BOOLEAN not null,
      account_non_expired BOOLEAN not null, 
      account_non_locked BOOLEAN not null, 
      credentials_non_expired BOOLEAN not null    
);

CREATE TABLE IF NOT EXISTS user_authority (
      user_uuid VARCHAR(64) not null,
      authority VARCHAR(255) not null,
      constraint fk_user_authority foreign key(user_uuid) references user(uuid),
      constraint ix_user_authority unique (user_uuid, authority)
);