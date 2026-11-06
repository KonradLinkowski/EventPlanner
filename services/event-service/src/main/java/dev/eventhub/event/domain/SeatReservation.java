package dev.eventhub.event.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** Idempotency ledger entry for a seat reservation made against an {@link Event}, keyed by booking id. */
@Entity
@Table(name = "seat_reservations")
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeatReservation {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "booking_id")
    private UUID bookingId;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private int seats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static SeatReservation reserved(UUID bookingId, UUID eventId, int seats) {
        SeatReservation reservation = new SeatReservation();
        reservation.bookingId = bookingId;
        reservation.eventId = eventId;
        reservation.seats = seats;
        reservation.status = ReservationStatus.RESERVED;
        return reservation;
    }

    public void release() {
        this.status = ReservationStatus.RELEASED;
    }

    public boolean isReleased() {
        return status == ReservationStatus.RELEASED;
    }
}
