CREATE TABLE IF NOT EXISTS social_user (
    user_id BIGINT NOT NULL,
    username VARCHAR(100) NOT NULL,
    invite_code VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY unique_username (username),
    UNIQUE KEY unique_invite_code (invite_code)
);

CREATE TABLE IF NOT EXISTS user_connection (
    id BIGINT AUTO_INCREMENT,
    inviter_id BIGINT NOT NULL,
    invitee_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_connection_pair (inviter_id, invitee_id)
);
