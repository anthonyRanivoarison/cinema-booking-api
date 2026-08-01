package io.poja.cinebook.entity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record Reservation(
    UUID id, Instant createdAt, UUID userId, UUID projectionId, List<UUID> seatIds) {}
