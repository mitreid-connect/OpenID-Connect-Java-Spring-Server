INSERT INTO system_scope (id, scope, description, icon, restricted, default_scope, structured, structured_param_description) VALUES
  (system_scope_seq.nextval, 'openid', 'log in using your identity', 'user', 0, 1, 0, null);

INSERT INTO system_scope (id, scope, description, icon, restricted, default_scope, structured, structured_param_description) VALUES
  (system_scope_seq.nextval, 'profile', 'basic profile information', 'list-alt', 0, 1, 0, null);

INSERT INTO system_scope (id, scope, description, icon, restricted, default_scope, structured, structured_param_description) VALUES
  (system_scope_seq.nextval, 'email', 'email address', 'envelope', 0, 1, 0, null);

INSERT INTO system_scope (id, scope, description, icon, restricted, default_scope, structured, structured_param_description) VALUES
  (system_scope_seq.nextval, 'address', 'physical address', 'home', 0, 1, 0, null);

INSERT INTO system_scope (id, scope, description, icon, restricted, default_scope, structured, structured_param_description) VALUES
  (system_scope_seq.nextval, 'phone', 'telephone number', 'bell', 0, 1, 0, null);

INSERT INTO system_scope (id, scope, description, icon, restricted, default_scope, structured, structured_param_description) VALUES
  (system_scope_seq.nextval, 'offline_access', 'offline access', 'time', 0, 0, 0, null);

INSERT INTO system_scope (id, scope, description, icon, restricted, default_scope, structured, structured_param_description) VALUES
  (system_scope_seq.nextval, 'online_access', 'offline access', 'time', 0, 0, 0, null);

INSERT INTO system_scope (id, scope, description, icon, restricted, default_scope, structured, structured_param_description) VALUES
  (system_scope_seq.nextval, 'launch/patient', 'Need patient context at launch time (FHIR Patient resource)', 'user', 0, 0, 0, null);

INSERT INTO system_scope (id, scope, description, icon, restricted, default_scope, structured, structured_param_description) VALUES
  (system_scope_seq.nextval, 'launch/encounter', 'Need encounter context at launch time (FHIR Encounter resource)', 'user', 0, 0, 0, null);

INSERT INTO system_scope (id, scope, description, icon, restricted, default_scope, structured, structured_param_description) VALUES
  (system_scope_seq.nextval, 'launch/location', 'Need location context at launch time (FHIR Location resource)', 'user', 0, 0, 0, null);

INSERT INTO system_scope (id, scope, description, icon, restricted, default_scope, structured, structured_param_description) VALUES
  (system_scope_seq.nextval, 'launch', 'Permission to obtain launch context when app is launched from an EHR', 'user', 0, 0, 0, null);
