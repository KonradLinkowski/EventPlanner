package dev.eventhub.event.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.eventhub.event.domain.exception.InvalidEventStateException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class EventTest {

    private Event newDraftEvent(int capacity) {
        return Event.draft(
                "organizer-1",
                "Spring Conf",
                "A conference about Spring",
                "TECH",
                new Venue("Arena", "Warsaw"),
                OffsetDateTime.now().plusDays(10),
                OffsetDateTime.now().plusDays(10).plusHours(3),
                new Money(new BigDecimal("99.90"), "PLN"),
                capacity);
    }

    @Test
    void draftStartsWithFullAvailability() {
        Event event = newDraftEvent(100);

        assertThat(event.getStatus()).isEqualTo(EventStatus.DRAFT);
        assertThat(event.getAvailableSeats()).isEqualTo(100);
    }

    @Test
    void publishTransitionsDraftToPublished() {
        Event event = newDraftEvent(100);

        event.publish();

        assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
    }

    @Test
    void publishingAlreadyPublishedEventFails() {
        Event event = newDraftEvent(100);
        event.publish();

        assertThatThrownBy(event::publish).isInstanceOf(InvalidEventStateException.class);
    }

    @Test
    void cancellingAlreadyCancelledEventFails() {
        Event event = newDraftEvent(100);
        event.cancel();

        assertThatThrownBy(event::cancel).isInstanceOf(InvalidEventStateException.class);
    }

    @Test
    void deletingEventWithNoSoldSeatsSucceeds() {
        Event event = newDraftEvent(100);

        event.markDeleted();

        assertThat(event.getDeletedAt()).isNotNull();
    }

    @Test
    void isOwnedByComparesOrganizerId() {
        Event event = newDraftEvent(100);

        assertThat(event.isOwnedBy("organizer-1")).isTrue();
        assertThat(event.isOwnedBy("someone-else")).isFalse();
    }
}
