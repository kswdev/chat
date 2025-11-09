CREATE TABLE IF NOT EXISTS user (
    user_id BIGINT AUTO_INCREMENT,
    user_name VARCHAR(20) NOT NULL,
    password VARCHAR(100) NOT NULL,
    invite_code VARCHAR(32) NOT NULL,
    connection_count INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY unique_user_name  (user_name),
    UNIQUE KEY unique_invite_code  (invite_code)
);

CREATE TABLE IF NOT EXISTS user_connection (
    partner_a_user_id BIGINT NOT NULL,
    partner_b_user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    inviter_user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (partner_a_user_id, partner_b_user_id),
    INDEX idx_partner_b_user_id (partner_b_user_id),
    INDEX idx_partner_a_user_id_status (partner_a_user_id, status),
    INDEX idx_partner_b_user_id_status (partner_b_user_id, status)
);

CREATE TABLE IF NOT EXISTS channel (
    channel_id BIGINT AUTO_INCREMENT,
    title VARCHAR(30) NOT NULL,
    invite_code VARCHAR(32) NOT NULL,
    head_count INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (channel_id),
    UNIQUE KEY unique_invite_code  (invite_code)
);

CREATE TABLE IF NOT EXISTS user_channel (
    user_id BIGINT NOT NULL ,
    channel_id BIGINT NOT NULL,
    last_read_msg_seq BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (user_id, channel_id),
    INDEX idx_channel_id (channel_id)
);