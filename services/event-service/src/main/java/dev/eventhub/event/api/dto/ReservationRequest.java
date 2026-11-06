package dev.eventhub.event.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

/** Request to reserve seats for a booking, idempotent by {@code bookingId}. */
public record ReservationRequest(@NotNull UUID bookingId, @Positive int seats) {}
