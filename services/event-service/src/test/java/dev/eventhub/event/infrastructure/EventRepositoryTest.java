package dev.eventhub.event.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.eventhub.event.TestcontainersConfiguration;
import dev.eventhub.event.domain.Event;
import dev.eventhub.event.domain.EventStatus;
import dev.eventhub.event.domain.Money;
import dev.eventhub.event.domain.Venue;
import dev.eventhub.event.domain.exception.EventDeletionNotAllowedException;
import dev.eventhub.event.domain.exception.InvalidCapacityException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Event persistPublishedEvent(String city, String category, int capacity) {
        Event event = Event.draft(
                "organizer-1",
                "Event in " + city,
                "desc",
                category,
                new Venue("Arena", city),
                OffsetDateTime.now().plusDays(10),
                OffsetDateTime.now().plusDays(10).plusHours(3),
                new Money(new BigDecimal("50.00"), "PLN"),
                capacity);
        event.publish();
        entityManager.persist(event);
        return event;
    }

    @Test
    void reserveSeatsDecrementsAvailabilityAtomicallyWhenEnoughSeats() {
        Event event = persistPublishedEvent("Warsaw", "TECH", 10);
        entityManager.flush();

        int updatedRows = eventRepository.reserveSeats(event.getId(), 4);
        entityManager.flush();
        entityManager.clear();

        assertThat(updatedRows).isEqualTo(1);
        assertThat(eventRepository.findById(event.getId()).orElseThrow().getAvailableSeats())
                .isEqualTo(6);
    }

    @Test
    void reserveSeatsFailsWhenNotEnoughSeatsLeft() {
        Event event = persistPublishedEvent("Warsaw", "TECH", 3);
        entityManager.flush();

        int updatedRows = eventRepository.reserveSeats(event.getId(), 5);
        entityManager.flush();
        entityManager.clear();

        assertThat(updatedRows).isEqualTo(0);
        assertThat(eventRepository.findById(event.getId()).orElseThrow().getAvailableSeats())
                .isEqualTo(3);
    }

    @Test
    void releaseSeatsIncreasesAvailability() {
        Event event = persistPublishedEvent("Warsaw", "TECH", 10);
        entityManager.flush();
        eventRepository.reserveSeats(event.getId(), 4);
        entityManager.flush();
        entityManager.clear();

        eventRepository.releaseSeats(event.getId(), 4);
        entityManager.flush();
        entityManager.clear();

        assertThat(eventRepository.findById(event.getId()).orElseThrow().getAvailableSeats())
                .isEqualTo(10);
    }

    @Test
    void capacityCannotBeReducedBelowSeatsSoldViaRealReservation() {
        Event event = persistPublishedEvent("Warsaw", "TECH", 100);
        entityManager.flush();
        eventRepository.reserveSeats(event.getId(), 60);
        entityManager.flush();
        entityManager.clear();

        Event reloaded = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(reloaded.getAvailableSeats()).isEqualTo(40);

        assertThatThrownBy(() -> reloaded.update(
                        reloaded.getTitle(),
                        reloaded.getDescription(),
                        reloaded.getCategory(),
                        reloaded.getVenue(),
                        reloaded.getStartsAt(),
                        reloaded.getEndsAt(),
                        reloaded.getPrice(),
                        50))
                .isInstanceOf(InvalidCapacityException.class);
    }

    @Test
    void deletingEventWithSoldSeatsIsForbidden() {
        Event event = persistPublishedEvent("Warsaw", "TECH", 100);
        entityManager.flush();
        eventRepository.reserveSeats(event.getId(), 10);
        entityManager.flush();
        entityManager.clear();

        Event reloaded = eventRepository.findById(event.getId()).orElseThrow();

        assertThatThrownBy(reloaded::markDeleted).isInstanceOf(EventDeletionNotAllowedException.class);
    }

    @Test
    void specificationFiltersByStatusCityAndCategory() {
        persistPublishedEvent("Warsaw", "TECH", 10);
        persistPublishedEvent("Krakow", "MUSIC", 10);
        entityManager.flush();
        entityManager.clear();

        var spec = EventSpecifications.hasStatus(EventStatus.PUBLISHED).and(EventSpecifications.cityEquals("Warsaw"));

        List<Event> results =
                eventRepository.findAll(spec, PageRequest.of(0, 10)).getContent();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getVenue().getCity()).isEqualTo("Warsaw");
    }
}
