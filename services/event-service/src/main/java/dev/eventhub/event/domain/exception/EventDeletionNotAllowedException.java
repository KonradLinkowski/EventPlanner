package dev.eventhub.event.domain.exception;

import java.util.UUID;

/** Thrown when an event cannot be deleted because it still has sold/reserved seats. */
public class EventDeletionNotAllowedException extends RuntimeException {

    public EventDeletionNotAllowedException(UUID eventId) {
        super("Event %s cannot be deleted: it has active seat reservations".formatted(eventId));
    }
}
