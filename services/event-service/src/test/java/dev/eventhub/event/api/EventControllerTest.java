package dev.eventhub.event.api;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.eventhub.event.api.dto.CreateEventRequest;
import dev.eventhub.event.api.dto.EventResponse;
import dev.eventhub.event.api.mapper.EventMapper;
import dev.eventhub.event.application.EventSearchCriteria;
import dev.eventhub.event.application.EventService;
import dev.eventhub.event.domain.Event;
import dev.eventhub.event.domain.EventStatus;
import dev.eventhub.event.domain.Money;
import dev.eventhub.event.domain.Venue;
import dev.eventhub.event.domain.exception.EventNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private EventMapper eventMapper;

    private EventResponse sampleResponse(UUID id) {
        return new EventResponse(
                id,
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
                100,
                100,
                EventStatus.DRAFT,
                Instant.now(),
                Instant.now());
    }

    @Test
    void getByIdReturnsEvent() throws Exception {
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
        when(eventService.getEvent(id)).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(sampleResponse(id));

        mockMvc.perform(get("/api/v1/events/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(id.toString())))
                .andExpect(jsonPath("$.title", equalTo("Spring Conf")));
    }

    @Test
    void getByIdReturnsNotFoundAsProblemDetail() throws Exception {
        UUID id = UUID.randomUUID();
        when(eventService.getEvent(id)).thenThrow(new EventNotFoundException(id));

        mockMvc.perform(get("/api/v1/events/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", equalTo("Event Not Found")));
    }

    @Test
    void searchReturnsPageOfEvents() throws Exception {
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
        Page<Event> page = new PageImpl<>(java.util.List.of(event));
        when(eventService.searchPublishedEvents(any(EventSearchCriteria.class), any(Pageable.class)))
                .thenReturn(page);
        when(eventMapper.toResponse(event)).thenReturn(sampleResponse(id));

        mockMvc.perform(get("/api/v1/events").param("city", "Warsaw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id", equalTo(id.toString())));
    }

    @Test
    void createReturnsValidationProblemForBlankTitle() throws Exception {
        CreateEventRequest request = new CreateEventRequest(
                "organizer-1",
                "",
                "desc",
                "TECH",
                "Warsaw",
                "Arena",
                OffsetDateTime.now().plusDays(10),
                OffsetDateTime.now().plusDays(10).plusHours(3),
                new BigDecimal("50.00"),
                "PLN",
                100);

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", equalTo("Validation Failed")));
    }
}
