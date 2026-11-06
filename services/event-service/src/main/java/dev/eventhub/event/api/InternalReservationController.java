package dev.eventhub.event.api;

import dev.eventhub.event.api.dto.ReservationRequest;
import dev.eventhub.event.api.dto.ReservationResponse;
import dev.eventhub.event.application.SeatReservationService;
import dev.eventhub.event.domain.SeatReservation;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Service-to-service seat reservation endpoints, not routed by the API Gateway. Operations are
 * idempotent, keyed by {@code bookingId}.
 */
@RestController
@RequiredArgsConstructor
public class InternalReservationController {

    private final SeatReservationService seatReservationService;

    @PostMapping("/internal/v1/events/{id}/reservations")
    public ResponseEntity<ReservationResponse> reserve(
            @PathVariable("id") UUID eventId, @Valid @RequestBody ReservationRequest request) {
        SeatReservation reservation =
                seatReservationService.reserveSeats(eventId, request.bookingId(), request.seats());
        return ResponseEntity.ok(toResponse(reservation));
    }

    @DeleteMapping("/internal/v1/events/{id}/reservations/{bookingId}")
    public ResponseEntity<Void> release(@PathVariable("id") UUID eventId, @PathVariable UUID bookingId) {
        seatReservationService.releaseSeats(eventId, bookingId);
        return ResponseEntity.noContent().build();
    }

    private ReservationResponse toResponse(SeatReservation reservation) {
        return new ReservationResponse(
                reservation.getBookingId(), reservation.getEventId(), reservation.getSeats(), reservation.getStatus());
    }
}
