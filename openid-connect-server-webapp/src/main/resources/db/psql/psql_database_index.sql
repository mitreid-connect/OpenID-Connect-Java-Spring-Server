--
-- Indexes for MySQL
--

CREATE INDEX at_tv_idx ON access_token(token_value;
CREATE INDEX ts_oi_idx ON token_scope(access_token_uuid);
CREATE INDEX at_exp_idx ON access_token(expiration);
CREATE INDEX rf_ahi_idx ON refresh_token(auth_holder_uuid);
CREATE INDEX rf_tv_idx ON refresh_token(token_value;
CREATE INDEX cd_ci_idx ON client_details(client_id);
CREATE INDEX at_ahi_idx ON access_token(auth_holder_uuid);
CREATE INDEX aha_oi_idx ON auth_holder_authority(auth_holder_uuid);
CREATE INDEX ahe_oi_idx ON auth_holder_extension(auth_holder_uuid);
CREATE INDEX ahrp_oi_idx ON auth_holder_request_parameter(auth_holder_uuid);
CREATE INDEX ahri_oi_idx ON auth_holder_resource_id(auth_holder_uuid);
CREATE INDEX ahrt_oi_idx ON auth_holder_resp_type(auth_holder_uuid);
CREATE INDEX ahs_oi_idx ON auth_holder_scope(auth_holder_uuid);
CREATE INDEX ac_ahi_idx ON authorization_code(auth_holder_uuid);
CREATE INDEX suaa_oi_idx ON saved_user_auth_authority(user_auth_uuid);
