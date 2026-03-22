CREATE TABLE ntf_notification_task
(
    id              BIGSERIAL PRIMARY KEY,
    task_no         VARCHAR(64)   NOT NULL UNIQUE,
    order_no        VARCHAR(64)   NOT NULL,
    task_type       VARCHAR(64)   NOT NULL,
    status          VARCHAR(32)   NOT NULL,
    attempt_count   INT           NOT NULL DEFAULT 0,
    last_error      VARCHAR(500),
    next_trigger_at TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ntf_task_status_next_trigger
    ON ntf_notification_task (status, next_trigger_at);
