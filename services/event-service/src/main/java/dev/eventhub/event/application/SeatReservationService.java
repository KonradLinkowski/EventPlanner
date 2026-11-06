package dev.eventhub.event.application;

import dev.eventhub.event.domain.SeatReservation;
import dev.eventhub.event.domain.exception.EventNotFoundException;
import dev.eventhub.event.domain.exception.InsufficientSeatsException;
import dev.eventhub.event.infrastructure.EventRepository;
import dev.eventhub.event.infrastructure.SeatReservationRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Idempotent seat reservation/release use cases for service-to-service calls (e.g. from Booking). */
@Service
@RequiredArgsConstructor
@Transactional
public class SeatReservationService {

    private final EventRepository eventRepository;
    private final SeatReservationRepository seatReservationRepository;

    public SeatReservation reserveSeats(UUID eventId, UUID bookingId, int seats) {
        return seatReservationRepository
                .findById(bookingId)
                .orElseGet(() -> createReservation(eventId, bookingId, seats));
    }

    /** No-op if the reservation does not exist or was already released (idempotent). */
    public void releaseSeats(UUID eventId, UUID bookingId) {
        seatReservationRepository.findById(bookingId).ifPresent(reservation -> {
            if (reservation.isReleased()) {
                return;
            }
            eventRepository.releaseSeats(eventId, reservation.getSeats());
            reservation.release();
        });
    }

    private SeatReservation createReservation(UUID eventId, UUID bookingId, int seats) {
        if (!eventRepository.existsById(eventId)) {
            throw new EventNotFoundException(eventId);
        }
        int updatedRows = eventRepository.reserveSeats(eventId, seats);
        if (updatedRows == 0) {
            throw new InsufficientSeatsException(eventId, seats);
        }
        return seatReservationRepository.save(SeatReservation.reserved(bookingId, eventId, seats));
    }
}
