package dev.eventhub.event.domain.exception;

import java.util.UUID;

/** Thrown when an event cannot be found by its id (or is soft-deleted). */
public class EventNotFoundException extends RuntimeException {

    public EventNotFoundException(UUID eventId) {
        super("Event not found: " + eventId);
    }
}
