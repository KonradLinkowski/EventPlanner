package dev.eventhub.event.application;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Application-layer command to update an existing event's details and/or capacity. */
public record UpdateEventCommand(
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
