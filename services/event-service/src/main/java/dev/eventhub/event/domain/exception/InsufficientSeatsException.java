package dev.eventhub.event.domain.exception;

import java.util.UUID;

/** Thrown when a seat reservation cannot be satisfied because not enough seats remain. */
public class InsufficientSeatsException extends RuntimeException {

    public InsufficientSeatsException(UUID eventId, int requested) {
        super("Event %s does not have %d available seat(s)".formatted(eventId, requested));
    }
}
