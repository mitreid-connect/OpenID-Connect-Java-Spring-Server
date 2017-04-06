--
-- Indexes for PostgreSQL
--

CREATE INDEX IF NOT EXISTS at_tv_idx ON access_token(token_value);
CREATE INDEX IF NOT EXISTS ts_oi_idx ON token_scope(owner_id);
CREATE INDEX IF NOT EXISTS at_exp_idx ON access_token(expiration);
CREATE INDEX IF NOT EXISTS rf_ahi_idx ON refresh_token(auth_holder_id);
CREATE INDEX IF NOT EXISTS cd_ci_idx ON client_details(client_id);
CREATE INDEX IF NOT EXISTS at_ahi_idx ON access_token(auth_holder_id);
CREATE INDEX IF NOT EXISTS aha_oi_idx ON authentication_holder_authority(owner_id);
CREATE INDEX IF NOT EXISTS ahe_oi_idx ON authentication_holder_extension(owner_id);
CREATE INDEX IF NOT EXISTS ahrp_oi_idx ON authentication_holder_request_parameter(owner_id);
CREATE INDEX IF NOT EXISTS ahri_oi_idx ON authentication_holder_resource_id(owner_id);
CREATE INDEX IF NOT EXISTS ahrt_oi_idx ON authentication_holder_response_type(owner_id);
CREATE INDEX IF NOT EXISTS ahs_oi_idx ON authentication_holder_scope(owner_id);
CREATE INDEX IF NOT EXISTS ac_ahi_idx ON authorization_code(auth_holder_id);
CREATE INDEX IF NOT EXISTS suaa_oi_idx ON saved_user_auth_authority(owner_id);
