package dev.eventhub.event.api.dto;

import dev.eventhub.event.domain.ReservationStatus;
import java.util.UUID;

/** Result of a seat reservation or release operation. */
public record ReservationResponse(UUID bookingId, UUID eventId, int seats, ReservationStatus status) {}
