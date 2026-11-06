package dev.eventhub.event.application;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Application-layer command to create a new event, independent of the API DTO shape. */
public record CreateEventCommand(
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
        int capacity) {}
