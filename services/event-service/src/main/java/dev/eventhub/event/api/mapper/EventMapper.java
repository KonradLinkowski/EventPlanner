package dev.eventhub.event.api.mapper;

import dev.eventhub.event.api.dto.EventResponse;
import dev.eventhub.event.domain.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "city", expression = "java(event.getVenue().getCity())")
    @Mapping(target = "venueName", expression = "java(event.getVenue().getName())")
    @Mapping(target = "priceAmount", expression = "java(event.getPrice().getAmount())")
    @Mapping(target = "currency", expression = "java(event.getPrice().getCurrency())")
    EventResponse toResponse(Event event);
}
