CREATE TABLE IF NOT EXISTS message (
    message_sequence BIGINT AUTO_INCREMENT,
    user_name VARCHAR(20) NOT NULL,
    content VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (message_sequence)
    );

CREATE TABLE IF NOT EXISTS user (
    user_id BIGINT AUTO_INCREMENT,
    user_name VARCHAR(20) NOT NULL,
    password VARCHAR(100) NOT NULL,
    connection_invite_code VARCHAR(20) NOT NULL,
    connection_count INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY unique_user_name  (user_name)
    UNIQUE KEY unique_connection_invite_code  (connection_invite_code)
    );

CREATE TABLE IF NOT EXISTS user_connection (
    partner_a_user_id BIGINT NOT NULL,
    partner_b_user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    inviter_user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (partner_a_user_id, partner_b_user_id)
    INDEX idx_partner_b_user_id (partner_b_user_id),
    INDEX idx_partner_a_user_id_status (partner_a_user_id, status),
    INDEX idx_partner_b_user_id_status (partner_b_user_id, status),
    );