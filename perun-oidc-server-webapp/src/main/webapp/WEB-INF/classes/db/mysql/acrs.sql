CREATE TABLE IF NOT EXISTS acrs (
    id BIGINT AUTO_INCREMENT,
    client_id VARCHAR(2048) NOT NULL,
    sub VARCHAR(2048) NOT NULL,
    state VARCHAR(2048) NOT NULL,
    shib_authn_context_class VARCHAR(2048) NOT NULL,
    expiration BIGINT NOT NULL,
    PRIMARY KEY (id)
);

ALTER TABLE  acrs MODIFY COLUMN expiration BIGINT;

CREATE TABLE IF NOT EXISTS device_code_acrs (
    id BIGINT AUTO_INCREMENT,
    device_code VARCHAR(2048) NOT NULL,
    user_code VARCHAR(2048) NOT NULL,
    shib_authn_context_class VARCHAR(2048),
    expiration BIGINT NOT NULL,
    PRIMARY KEY (id)
);

ALTER TABLE device_code_acrs MODIFY COLUMN expiration BIGINT;
