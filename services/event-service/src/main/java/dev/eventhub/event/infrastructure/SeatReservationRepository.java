package dev.eventhub.event.infrastructure;

import dev.eventhub.event.domain.SeatReservation;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatReservationRepository extends JpaRepository<SeatReservation, UUID> {}
