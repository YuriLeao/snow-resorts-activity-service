-- activity schema: descent sessions, final metrics, raw GPS points and S3 track refs.
-- gps_points uses a plain Postgres table in dev; TimescaleDB hypertable is opt-in for staging/prod.

CREATE TABLE runs (
    id         UUID PRIMARY KEY,
    user_id    UUID NOT NULL,
    resort_id  UUID,
    trail_id   UUID,
    started_at TIMESTAMPTZ NOT NULL,
    ended_at   TIMESTAMPTZ,
    status     VARCHAR(20) NOT NULL  -- ACTIVE | COMPLETED | DISCARDED
);
CREATE INDEX idx_runs_user ON runs (user_id);
CREATE INDEX idx_runs_user_started ON runs (user_id, started_at DESC);

CREATE TABLE run_metrics (
    run_id              UUID PRIMARY KEY REFERENCES runs(id) ON DELETE CASCADE,
    max_speed_kmh       DOUBLE PRECISION NOT NULL DEFAULT 0,
    avg_speed_kmh       DOUBLE PRECISION NOT NULL DEFAULT 0,
    distance_m          DOUBLE PRECISION NOT NULL DEFAULT 0,
    vertical_drop_m     DOUBLE PRECISION NOT NULL DEFAULT 0,
    max_inclination_deg DOUBLE PRECISION NOT NULL DEFAULT 0,
    avg_inclination_deg DOUBLE PRECISION NOT NULL DEFAULT 0,
    duration_sec        BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE gps_points (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    run_id      UUID NOT NULL REFERENCES runs(id) ON DELETE CASCADE,
    recorded_at TIMESTAMPTZ NOT NULL,
    lat         DOUBLE PRECISION NOT NULL,
    lng         DOUBLE PRECISION NOT NULL,
    altitude    DOUBLE PRECISION,
    speed_kmh   DOUBLE PRECISION,
    inclination DOUBLE PRECISION
);
CREATE INDEX idx_gps_points_run ON gps_points (run_id, recorded_at);

CREATE TABLE run_tracks_s3 (
    run_id      UUID PRIMARY KEY REFERENCES runs(id) ON DELETE CASCADE,
    s3_key      VARCHAR(512) NOT NULL,
    format      VARCHAR(20)  NOT NULL,
    point_count INTEGER      NOT NULL
);
