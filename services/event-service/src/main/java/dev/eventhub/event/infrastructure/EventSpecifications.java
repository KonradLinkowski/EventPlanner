package dev.eventhub.event.infrastructure;

import dev.eventhub.event.domain.Event;
import dev.eventhub.event.domain.EventStatus;
import java.time.OffsetDateTime;
import org.springframework.data.jpa.domain.Specification;

/** Reusable {@link Specification} filters for querying events. */
public final class EventSpecifications {

    private EventSpecifications() {}

    public static Specification<Event> hasStatus(EventStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Event> hasOrganizerId(String organizerId) {
        return (root, query, cb) -> cb.equal(root.get("organizerId"), organizerId);
    }

    public static Specification<Event> cityEquals(String city) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("venue").get("city")), city.toLowerCase());
    }

    public static Specification<Event> categoryEquals(String category) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("category")), category.toLowerCase());
    }

    public static Specification<Event> startsAtFrom(OffsetDateTime from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startsAt"), from);
    }

    public static Specification<Event> startsAtTo(OffsetDateTime to) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("startsAt"), to);
    }
}
