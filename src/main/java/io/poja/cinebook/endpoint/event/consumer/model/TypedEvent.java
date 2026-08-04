package io.poja.cinebook.endpoint.event.consumer.model;

import io.poja.cinebook.PojaGenerated;
import io.poja.cinebook.endpoint.event.model.PojaEvent;

@PojaGenerated
public record TypedEvent(String typeName, PojaEvent payload) {}
