package dev.eventhub.event.api.dto;

import dev.eventhub.event.application.CreateEventCommand;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Request to create a new (DRAFT) event.
 *
 * <p>{@code organizerId} is accepted explicitly until Stage 5 wires up JWT-based identity.
 */
public record CreateEventRequest(
        @NotBlank String organizerId,
        @NotBlank @Size(max = 255) String title,
        @Size(max = 4000) String description,
        @NotBlank @Size(max = 100) String category,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 255) String venueName,
        @NotNull OffsetDateTime startsAt,
        @NotNull OffsetDateTime endsAt,
        @NotNull @DecimalMin(value = "0.00") BigDecimal priceAmount,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @Positive int capacity) {

    @AssertTrue(message = "endsAt must be after startsAt")
    public boolean isEndAfterStart() {
        return startsAt == null || endsAt == null || endsAt.isAfter(startsAt);
    }

    public CreateEventCommand toCommand() {
        return new CreateEventCommand(
                organizerId,
                title,
                description,
                category,
                city,
                venueName,
                startsAt,
                endsAt,
                priceAmount,
                currency,
                capacity);
    }
}
