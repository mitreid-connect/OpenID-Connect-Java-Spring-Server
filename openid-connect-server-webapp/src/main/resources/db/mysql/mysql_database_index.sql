--
-- Indexes for MySQL
--

CREATE INDEX at_tv_idx ON access_token(token_value(767));
CREATE INDEX ts_oi_idx ON token_scope(owner_id);
CREATE INDEX at_exp_idx ON access_token(expiration);
CREATE INDEX rf_ahi_idx ON refresh_token(auth_holder_id);
CREATE INDEX cd_ci_idx ON client_details(client_id);
CREATE INDEX at_ahi_idx ON access_token(auth_holder_id);
CREATE INDEX aha_oi_idx ON authentication_holder_authority(owner_id);
CREATE INDEX ahe_oi_idx ON authentication_holder_extension(owner_id);
CREATE INDEX ahrp_oi_idx ON authentication_holder_request_parameter(owner_id);
CREATE INDEX ahri_oi_idx ON authentication_holder_resource_id(owner_id);
CREATE INDEX ahrt_oi_idx ON authentication_holder_response_type(owner_id);
CREATE INDEX ahs_oi_idx ON authentication_holder_scope(owner_id);
CREATE INDEX ac_ahi_idx ON authorization_code(auth_holder_id);
CREATE INDEX suaa_oi_idx ON saved_user_auth_authority(owner_id);
