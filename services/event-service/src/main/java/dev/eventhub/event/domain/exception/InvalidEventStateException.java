package dev.eventhub.event.domain.exception;

/** Thrown when a requested status transition is not allowed from the event's current state. */
public class InvalidEventStateException extends RuntimeException {

    public InvalidEventStateException(String message) {
        super(message);
    }
}
