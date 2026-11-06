package dev.eventhub.event.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.eventhub.event.domain.ReservationStatus;
import dev.eventhub.event.domain.SeatReservation;
import dev.eventhub.event.domain.exception.EventNotFoundException;
import dev.eventhub.event.domain.exception.InsufficientSeatsException;
import dev.eventhub.event.infrastructure.EventRepository;
import dev.eventhub.event.infrastructure.SeatReservationRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SeatReservationServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private SeatReservationRepository seatReservationRepository;

    private SeatReservationService seatReservationService;

    @BeforeEach
    void setUp() {
        seatReservationService = new SeatReservationService(eventRepository, seatReservationRepository);
    }

    @Test
    void reserveSeatsIsIdempotentForKnownBookingId() {
        UUID eventId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        SeatReservation existing = SeatReservation.reserved(bookingId, eventId, 2);
        when(seatReservationRepository.findById(bookingId)).thenReturn(Optional.of(existing));

        SeatReservation result = seatReservationService.reserveSeats(eventId, bookingId, 2);

        assertThat(result).isSameAs(existing);
        verify(eventRepository, never()).reserveSeats(any(), anyInt());
    }

    @Test
    void reserveSeatsThrowsWhenEventMissing() {
        UUID eventId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        when(seatReservationRepository.findById(bookingId)).thenReturn(Optional.empty());
        when(eventRepository.existsById(eventId)).thenReturn(false);

        assertThatThrownBy(() -> seatReservationService.reserveSeats(eventId, bookingId, 2))
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    void reserveSeatsThrowsWhenNotEnoughSeats() {
        UUID eventId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        when(seatReservationRepository.findById(bookingId)).thenReturn(Optional.empty());
        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(eventRepository.reserveSeats(eventId, 5)).thenReturn(0);

        assertThatThrownBy(() -> seatReservationService.reserveSeats(eventId, bookingId, 5))
                .isInstanceOf(InsufficientSeatsException.class);
    }

    @Test
    void reserveSeatsSucceedsAndPersistsReservation() {
        UUID eventId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        when(seatReservationRepository.findById(bookingId)).thenReturn(Optional.empty());
        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(eventRepository.reserveSeats(eventId, 3)).thenReturn(1);
        when(seatReservationRepository.save(any(SeatReservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SeatReservation result = seatReservationService.reserveSeats(eventId, bookingId, 3);

        assertThat(result.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(result.getSeats()).isEqualTo(3);
    }

    @Test
    void releaseSeatsIsNoOpWhenReservationMissing() {
        UUID eventId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        when(seatReservationRepository.findById(bookingId)).thenReturn(Optional.empty());

        seatReservationService.releaseSeats(eventId, bookingId);

        verify(eventRepository, never()).releaseSeats(any(), anyInt());
    }

    @Test
    void releaseSeatsIsNoOpWhenAlreadyReleased() {
        UUID eventId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        SeatReservation reservation = SeatReservation.reserved(bookingId, eventId, 2);
        reservation.release();
        when(seatReservationRepository.findById(bookingId)).thenReturn(Optional.of(reservation));

        seatReservationService.releaseSeats(eventId, bookingId);

        verify(eventRepository, never()).releaseSeats(any(), anyInt());
    }

    @Test
    void releaseSeatsReleasesAndUpdatesAvailability() {
        UUID eventId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        SeatReservation reservation = SeatReservation.reserved(bookingId, eventId, 2);
        when(seatReservationRepository.findById(bookingId)).thenReturn(Optional.of(reservation));

        seatReservationService.releaseSeats(eventId, bookingId);

        verify(eventRepository).releaseSeats(eventId, 2);
        assertThat(reservation.isReleased()).isTrue();
    }
}
