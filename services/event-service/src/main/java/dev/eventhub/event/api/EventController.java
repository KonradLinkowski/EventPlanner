package dev.eventhub.event.api;

import dev.eventhub.event.api.dto.CreateEventRequest;
import dev.eventhub.event.api.dto.EventResponse;
import dev.eventhub.event.api.dto.UpdateEventRequest;
import dev.eventhub.event.api.mapper.EventMapper;
import dev.eventhub.event.application.EventSearchCriteria;
import dev.eventhub.event.application.EventService;
import dev.eventhub.event.domain.Event;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final EventMapper eventMapper;

    @GetMapping("/api/v1/events")
    public Page<EventResponse> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to,
            Pageable pageable) {
        EventSearchCriteria criteria = new EventSearchCriteria(city, category, from, to);
        return eventService.searchPublishedEvents(criteria, pageable).map(eventMapper::toResponse);
    }

    @GetMapping("/api/v1/events/{id}")
    public EventResponse getById(@PathVariable UUID id) {
        return eventMapper.toResponse(eventService.getEvent(id));
    }

    @PostMapping("/api/v1/events")
    public ResponseEntity<EventResponse> create(@Valid @RequestBody CreateEventRequest request) {
        Event event = eventService.createEvent(request.toCommand());
        EventResponse response = eventMapper.toResponse(event);
        return ResponseEntity.created(URI.create("/api/v1/events/" + response.id()))
                .body(response);
    }

    @PutMapping("/api/v1/events/{id}")
    public EventResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateEventRequest request) {
        return eventMapper.toResponse(eventService.updateEvent(id, request.toCommand()));
    }

    @PostMapping("/api/v1/events/{id}/publish")
    public EventResponse publish(@PathVariable UUID id) {
        return eventMapper.toResponse(eventService.publishEvent(id));
    }

    @PostMapping("/api/v1/events/{id}/cancel")
    public EventResponse cancel(@PathVariable UUID id) {
        return eventMapper.toResponse(eventService.cancelEvent(id));
    }

    @DeleteMapping("/api/v1/events/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/organizer/events")
    public Page<EventResponse> listOrganizerEvents(@RequestParam String organizerId, Pageable pageable) {
        return eventService.listOrganizerEvents(organizerId, pageable).map(eventMapper::toResponse);
    }
}
