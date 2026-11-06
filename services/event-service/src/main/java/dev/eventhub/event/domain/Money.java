package dev.eventhub.event.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Value object for a monetary amount in a given ISO-4217 currency. */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class Money {

    @Column(name = "price_amount", nullable = false)
    private final BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private final String currency;
}
