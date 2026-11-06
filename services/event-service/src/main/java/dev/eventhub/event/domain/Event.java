package dev.eventhub.event.domain;

import dev.eventhub.event.domain.exception.EventDeletionNotAllowedException;
import dev.eventhub.event.domain.exception.InvalidCapacityException;
import dev.eventhub.event.domain.exception.InvalidEventStateException;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

/** Event aggregate root. Owns its seat inventory; seat reservations are tracked separately. */
@Entity
@Table(name = "events")
@SQLRestriction("deleted_at IS NULL")
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "organizer_id", nullable = false)
    private String organizerId;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Column(nullable = false)
    private String category;

    @Embedded
    private Venue venue;

    @Column(name = "starts_at", nullable = false)
    private OffsetDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private OffsetDateTime endsAt;

    @Embedded
    private Money price;

    @Column(nullable = false)
    private int capacity;

    @Column(name = "available_seats", nullable = false)
    private int availableSeats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    private long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static Event draft(
            String organizerId,
            String title,
            String description,
            String category,
            Venue venue,
            OffsetDateTime startsAt,
            OffsetDateTime endsAt,
            Money price,
            int capacity) {
        Event event = new Event();
        event.organizerId = organizerId;
        event.title = title;
        event.description = description;
        event.category = category;
        event.venue = venue;
        event.startsAt = startsAt;
        event.endsAt = endsAt;
        event.price = price;
        event.capacity = capacity;
        event.availableSeats = capacity;
        event.status = EventStatus.DRAFT;
        return event;
    }

    public void update(
            String title,
            String description,
            String category,
            Venue venue,
            OffsetDateTime startsAt,
            OffsetDateTime endsAt,
            Money price,
            int newCapacity) {
        int sold = capacity - availableSeats;
        if (newCapacity < sold) {
            throw new InvalidCapacityException(
                    "Cannot reduce capacity to %d: %d seat(s) already sold".formatted(newCapacity, sold));
        }
        this.title = title;
        this.description = description;
        this.category = category;
        this.venue = venue;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.price = price;
        this.availableSeats = newCapacity - sold;
        this.capacity = newCapacity;
    }

    public void publish() {
        if (status != EventStatus.DRAFT) {
            throw new InvalidEventStateException("Only DRAFT events can be published, current status: " + status);
        }
        this.status = EventStatus.PUBLISHED;
    }

    public void cancel() {
        if (status == EventStatus.CANCELLED) {
            throw new InvalidEventStateException("Event is already cancelled");
        }
        this.status = EventStatus.CANCELLED;
    }

    public void markDeleted() {
        if (availableSeats < capacity) {
            throw new EventDeletionNotAllowedException(id);
        }
        this.deletedAt = Instant.now();
    }

    public boolean isOwnedBy(String candidateOrganizerId) {
        return organizerId.equals(candidateOrganizerId);
    }
}
