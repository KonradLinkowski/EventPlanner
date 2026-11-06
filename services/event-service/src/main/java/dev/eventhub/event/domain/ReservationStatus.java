package dev.eventhub.event.domain;

/** Status of a seat reservation ledger entry, keyed by booking id for idempotency. */
public enum ReservationStatus {
    RESERVED,
    RELEASED
}
