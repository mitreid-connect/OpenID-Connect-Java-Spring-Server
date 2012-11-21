INSERT INTO users(username, password, enabled) values ('jricher','password',true);
INSERT INTO authorities(username,authority) values ('jricher','ROLE_USER');
INSERT INTO authorities(username,authority) values ('jricher','ROLE_ADMIN');
INSERT INTO user_info(user_id, preferred_username, name, email_verified) values ('jricher','jricher','jricher', 'FALSE');

INSERT INTO users(username, password, enabled) values ('mfranklin','password',true);
INSERT INTO authorities(username,authority) values ('mfranklin','ROLE_USER');
INSERT INTO authorities(username,authority) values ('mfranklin','ROLE_ADMIN');

INSERT INTO users(username, password, enabled) values ('dcuomo','password',true);
INSERT INTO authorities(username,authority) values ('dcuomo','ROLE_USER');
INSERT INTO authorities(username,authority) values ('dcuomo','ROLE_ADMIN');

INSERT INTO users(username, password, enabled) values ('aanganes','password',true);
INSERT INTO authorities(username,authority) values ('aanganes','ROLE_USER');
INSERT INTO authorities(username,authority) values ('aanganes','ROLE_ADMIN');

INSERT INTO users(username, password, enabled) values ('mjwalsh','password',true);
INSERT INTO authorities(username,authority) values ('mjwalsh','ROLE_USER');
INSERT INTO authorities(username,authority) values ('mjwalsh','ROLE_ADMIN');

INSERT INTO users(username, password, enabled) values ('srmoore','password',true);
INSERT INTO authorities(username,authority) values ('srmoore','ROLE_USER');
INSERT INTO authorities(username,authority) values ('srmoore','ROLE_ADMIN');