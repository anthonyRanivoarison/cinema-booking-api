package io.poja.cinebook.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record Projection(
    UUID id, Instant datetime, BigDecimal seatPrice, UUID movieId, UUID roomId) {}
