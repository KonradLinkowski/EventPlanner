package dev.eventhub.event.api.dto;

import dev.eventhub.event.application.UpdateEventCommand;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Request to update an existing event's details and/or capacity. */
public record UpdateEventRequest(
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

    public UpdateEventCommand toCommand() {
        return new UpdateEventCommand(
                title, description, category, city, venueName, startsAt, endsAt, priceAmount, currency, capacity);
    }
}
