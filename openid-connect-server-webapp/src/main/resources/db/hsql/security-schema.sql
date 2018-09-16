-- 
-- Tables for Spring Security's user details service
--
  
CREATE TABLE IF NOT EXISTS users(
	  uuid VARCHAR(64) PRIMARY KEY,
	  host_uuid VARCHAR(64),
      username VARCHAR(255) not null,
      password VARCHAR(255) not null,
      enabled BOOLEAN not null)           
;

CREATE TABLE IF NOT EXISTS authorities (
      uuid VARCHAR(64) PRIMARY not null,
      host_id BIGINT,
      user_uuid VARCHAR(64) PRIMARY not null,
      authority VARCHAR(255) not null,
      UNIQUE KEY ix_authorities (uuid, authority),
      CONSTRAINT authorities_users_FK FOREIGN KEY (user_uuid) REFERENCES users (uuid)
;