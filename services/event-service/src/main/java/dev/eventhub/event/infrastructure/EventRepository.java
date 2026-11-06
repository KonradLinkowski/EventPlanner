package dev.eventhub.event.infrastructure;

import dev.eventhub.event.domain.Event;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, UUID>, JpaSpecificationExecutor<Event> {

    /**
     * Atomically decrements available seats if enough remain. Returns the number of rows updated
     * (0 means not found or not enough seats left).
     */
    @Modifying
    @Query(
            """
            UPDATE Event e SET e.availableSeats = e.availableSeats - :seats, e.version = e.version + 1
            WHERE e.id = :eventId AND e.availableSeats >= :seats
            """)
    int reserveSeats(@Param("eventId") UUID eventId, @Param("seats") int seats);

    /** Atomically releases previously reserved seats back into the available pool. */
    @Modifying
    @Query(
            """
            UPDATE Event e SET e.availableSeats = e.availableSeats + :seats, e.version = e.version + 1
            WHERE e.id = :eventId
            """)
    int releaseSeats(@Param("eventId") UUID eventId, @Param("seats") int seats);
}
