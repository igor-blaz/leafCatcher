CREATE SCHEMA IF NOT EXISTS schema;
SET search_path TO schema;

CREATE TABLE endings
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    author_id   BIGINT NOT NULL,
    event_uuid  UUID,
    notice      TEXT,
    button_name TEXT,
    description TEXT
);

CREATE INDEX idx_endings_author_id ON endings (author_id);
