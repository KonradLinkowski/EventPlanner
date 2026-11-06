package dev.eventhub.event.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.eventhub.event.domain.Event;
import dev.eventhub.event.domain.EventStatus;
import dev.eventhub.event.domain.Money;
import dev.eventhub.event.domain.Venue;
import dev.eventhub.event.domain.exception.EventNotFoundException;
import dev.eventhub.event.infrastructure.EventRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventService(eventRepository);
    }

    private CreateEventCommand createCommand() {
        return new CreateEventCommand(
                "organizer-1",
                "Spring Conf",
                "desc",
                "TECH",
                "Warsaw",
                "Arena",
                OffsetDateTime.now().plusDays(10),
                OffsetDateTime.now().plusDays(10).plusHours(3),
                new BigDecimal("50.00"),
                "PLN",
                100);
    }

    @Test
    void createEventSavesDraftEvent() {
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event event = eventService.createEvent(createCommand());

        assertThat(event.getStatus()).isEqualTo(EventStatus.DRAFT);
        assertThat(event.getVenue()).isEqualTo(new Venue("Arena", "Warsaw"));
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void getEventThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(eventRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEvent(id)).isInstanceOf(EventNotFoundException.class);
    }

    @Test
    void publishEventDelegatesToDomain() {
        UUID id = UUID.randomUUID();
        Event event = Event.draft(
                "organizer-1",
                "Spring Conf",
                "desc",
                "TECH",
                new Venue("Arena", "Warsaw"),
                OffsetDateTime.now().plusDays(10),
                OffsetDateTime.now().plusDays(10).plusHours(3),
                new Money(new BigDecimal("50.00"), "PLN"),
                100);
        when(eventRepository.findById(id)).thenReturn(Optional.of(event));

        Event published = eventService.publishEvent(id);

        assertThat(published.getStatus()).isEqualTo(EventStatus.PUBLISHED);
    }
}
