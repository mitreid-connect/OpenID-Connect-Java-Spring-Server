-- 
-- Tables for Spring Security's user details service
--
  
create table IF NOT EXISTS users(
      username varchar(50) not null primary key,
      password varchar(50) not null,
      enabled boolean not null);

  create table IF NOT EXISTS authorities (
      username varchar(50) not null,
      authority varchar(50) not null,
      constraint fk_authorities_users foreign key(username) references users(username),
      constraint ix_authority unique (username,authority));