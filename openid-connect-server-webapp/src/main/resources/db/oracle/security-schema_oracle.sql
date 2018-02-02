-- 
-- Tables for Spring Security's user details service
--
  
create table users(
  username varchar2(50) not null primary key,
  password varchar2(50) not null,
  enabled number(1) not null,

  constraint enabled_check check (enabled in (1, 0))
);

create table authorities (
  username varchar2(50) not null,
  authority varchar2(50) not null,
  constraint fk_authorities_users foreign key(username) references users(username),
  constraint ix_authority unique (username,authority)
);
