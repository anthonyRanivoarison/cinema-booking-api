package io.poja.cinebook.entity;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record Room(UUID id, String number, int capacity, List<Seat> seats) {}
