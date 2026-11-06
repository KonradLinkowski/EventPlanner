package dev.eventhub.event.api.dto;

import dev.eventhub.event.domain.EventStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Public representation of an event. */
public record EventResponse(
        UUID id,
        String organizerId,
        String title,
        String description,
        String category,
        String city,
        String venueName,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        BigDecimal priceAmount,
        String currency,
        int capacity,
        int availableSeats,
        EventStatus status,
        Instant createdAt,
        Instant updatedAt) {}
