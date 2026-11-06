package dev.eventhub.event.application;

import dev.eventhub.event.domain.Event;
import dev.eventhub.event.domain.EventStatus;
import dev.eventhub.event.domain.Money;
import dev.eventhub.event.domain.Venue;
import dev.eventhub.event.domain.exception.EventNotFoundException;
import dev.eventhub.event.infrastructure.EventRepository;
import dev.eventhub.event.infrastructure.EventSpecifications;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Use cases for creating, editing and browsing events. */
@Service
@RequiredArgsConstructor
@Transactional
public class EventService {

    private final EventRepository eventRepository;

    public Event createEvent(CreateEventCommand command) {
        Event event = Event.draft(
                command.organizerId(),
                command.title(),
                command.description(),
                command.category(),
                new Venue(command.venueName(), command.city()),
                command.startsAt(),
                command.endsAt(),
                new Money(command.priceAmount(), command.currency()),
                command.capacity());
        return eventRepository.save(event);
    }

    public Event updateEvent(UUID eventId, UpdateEventCommand command) {
        Event event = getEvent(eventId);
        event.update(
                command.title(),
                command.description(),
                command.category(),
                new Venue(command.venueName(), command.city()),
                command.startsAt(),
                command.endsAt(),
                new Money(command.priceAmount(), command.currency()),
                command.capacity());
        return event;
    }

    public Event publishEvent(UUID eventId) {
        Event event = getEvent(eventId);
        event.publish();
        return event;
    }

    public Event cancelEvent(UUID eventId) {
        Event event = getEvent(eventId);
        event.cancel();
        return event;
    }

    public void deleteEvent(UUID eventId) {
        Event event = getEvent(eventId);
        event.markDeleted();
    }

    @Transactional(readOnly = true)
    public Event getEvent(UUID eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
    }

    @Transactional(readOnly = true)
    public Page<Event> searchPublishedEvents(EventSearchCriteria criteria, Pageable pageable) {
        Specification<Event> spec = EventSpecifications.hasStatus(EventStatus.PUBLISHED);
        if (criteria.city() != null && !criteria.city().isBlank()) {
            spec = spec.and(EventSpecifications.cityEquals(criteria.city()));
        }
        if (criteria.category() != null && !criteria.category().isBlank()) {
            spec = spec.and(EventSpecifications.categoryEquals(criteria.category()));
        }
        if (criteria.from() != null) {
            spec = spec.and(EventSpecifications.startsAtFrom(criteria.from()));
        }
        if (criteria.to() != null) {
            spec = spec.and(EventSpecifications.startsAtTo(criteria.to()));
        }
        return eventRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Event> listOrganizerEvents(String organizerId, Pageable pageable) {
        return eventRepository.findAll(EventSpecifications.hasOrganizerId(organizerId), pageable);
    }
}
