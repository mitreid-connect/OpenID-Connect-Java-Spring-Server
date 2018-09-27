-- 
-- Indexes for HSQLDB
-- 

CREATE INDEX IF NOT EXISTS at_tv_idx ON access_token(token_value);
CREATE INDEX IF NOT EXISTS ts_atu_idx ON token_scope(access_token_uuid);
CREATE INDEX IF NOT EXISTS at_exp_idx ON access_token(expiration);
CREATE INDEX IF NOT EXISTS rf_ahu_idx ON refresh_token(auth_holder_uuid);
CREATE INDEX IF NOT EXISTS rf_tv_idx ON refresh_token(token_value);
CREATE INDEX IF NOT EXISTS cd_ci_idx ON client_details(client_id);
CREATE INDEX IF NOT EXISTS at_ahu_idx ON access_token(auth_holder_uuid);
CREATE INDEX IF NOT EXISTS aha_ahu_idx ON authentication_holder_authority(auth_holder_uuid);
CREATE INDEX IF NOT EXISTS ahe_ahu_idx ON authentication_holder_extension(auth_holder_uuid);
CREATE INDEX IF NOT EXISTS ahrp_ahu_idx ON authentication_holder_request_parameter(auth_holder_uuid);
CREATE INDEX IF NOT EXISTS ahri_ahu_idx ON authentication_holder_resource_id(auth_holder_uuid);
CREATE INDEX IF NOT EXISTS ahrt_ahu_idx ON authentication_holder_response_type(auth_holder_uuid);
CREATE INDEX IF NOT EXISTS ahs_ahu_idx ON authentication_holder_scope(auth_holder_uuid);
CREATE INDEX IF NOT EXISTS ac_ahu_idx ON authorization_code(auth_holder_uuid);
CREATE INDEX IF NOT EXISTS suaa_suau_idx ON saved_user_auth_authority(user_auth_uuid);
