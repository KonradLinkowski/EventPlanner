CREATE TABLE events (
    id               UUID PRIMARY KEY,
    organizer_id     VARCHAR(255)   NOT NULL,
    title            VARCHAR(255)   NOT NULL,
    description      TEXT,
    category         VARCHAR(100)   NOT NULL,
    city             VARCHAR(100)   NOT NULL,
    venue_name       VARCHAR(255)   NOT NULL,
    starts_at        TIMESTAMPTZ    NOT NULL,
    ends_at          TIMESTAMPTZ    NOT NULL,
    price_amount     NUMERIC(10, 2) NOT NULL,
    currency         VARCHAR(3)     NOT NULL,
    capacity         INT            NOT NULL CHECK (capacity > 0),
    available_seats  INT            NOT NULL CHECK (available_seats >= 0),
    status           VARCHAR(20)    NOT NULL CHECK (status IN ('DRAFT', 'PUBLISHED', 'CANCELLED')),
    deleted_at       TIMESTAMPTZ,
    version          BIGINT         NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CHECK (ends_at > starts_at),
    CHECK (available_seats <= capacity)
);

CREATE INDEX idx_events_city ON events (city);
CREATE INDEX idx_events_category ON events (category);
CREATE INDEX idx_events_starts_at ON events (starts_at);
CREATE INDEX idx_events_status ON events (status);
CREATE INDEX idx_events_organizer_id ON events (organizer_id);

CREATE TABLE seat_reservations (
    booking_id  UUID PRIMARY KEY,
    event_id    UUID        NOT NULL REFERENCES events (id),
    seats       INT         NOT NULL CHECK (seats > 0),
    status      VARCHAR(20) NOT NULL CHECK (status IN ('RESERVED', 'RELEASED')),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_seat_reservations_event_id ON seat_reservations (event_id);
