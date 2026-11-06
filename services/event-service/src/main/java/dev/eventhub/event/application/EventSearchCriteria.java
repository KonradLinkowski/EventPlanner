package dev.eventhub.event.application;

import java.time.OffsetDateTime;

/** Optional filters for searching published events. Null fields are ignored. */
public record EventSearchCriteria(String city, String category, OffsetDateTime from, OffsetDateTime to) {}
