package io.poja.cinebook.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ProjectionSummary(
    UUID id, Instant datetime, BigDecimal seatPrice, MovieSummary movie, RoomSummary room) {}
