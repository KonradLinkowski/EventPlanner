package dev.eventhub.event.domain.exception;

/** Thrown when capacity would be reduced below the number of seats already sold. */
public class InvalidCapacityException extends RuntimeException {

    public InvalidCapacityException(String message) {
        super(message);
    }
}
