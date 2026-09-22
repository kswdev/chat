CREATE TABLE IF NOT EXISTS user_connection_count (
    user_id BIGINT NOT NULL,
    connection_count INT NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id)
);

CREATE TABLE IF NOT EXISTS user_connection (
    id BIGINT AUTO_INCREMENT,
    inviter_id BIGINT NOT NULL,
    invitee_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    user_id_a BIGINT NOT NULL,
    user_id_b BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_connection_pair (inviter_id, invitee_id),
    INDEX idx_user_connection_a_status (user_id_a, status),
    INDEX idx_user_connection_b_status (user_id_b, status)
);
