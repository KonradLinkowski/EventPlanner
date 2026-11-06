package dev.eventhub.event.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Value object for where an event takes place. */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class Venue {

    @Column(name = "venue_name", nullable = false)
    private final String name;

    @Column(name = "city", nullable = false)
    private final String city;
}
