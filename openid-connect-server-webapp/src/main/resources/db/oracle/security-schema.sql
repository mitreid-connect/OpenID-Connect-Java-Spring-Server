-- 
-- Tables for Spring Security's user details service
--

CREATE TABLE user_ (
  uuid VARCHAR(64) PRIMARY KEY,
  host_uuid VARCHAR2(64),
  username VARCHAR2(255) not null,
  password VARCHAR2(255) not null,
  enabled NUMBER(1,0)  not null,
  account_non_expired NUMBER(1,0) not null, 
  account_non_locked NUMBER(1,0) not null, 
  credentials_non_expired NUMBER(1,0) not null,   
    
  CONSTRAINT ENABLED_CHECK CHECK (ENABLED IN (1, 0))
);

CREATE TABLE user_authority (
  user_uuid VARCHAR2(64) not null,
  authority VARCHAR2(255) not null,
  constraint fk_authorities_users foreign key(user_uuid) references user_(uuid),
  constraint ix_authority unique (user_uuid, authority)
);
