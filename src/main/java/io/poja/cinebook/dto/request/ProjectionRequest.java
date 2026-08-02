package io.poja.cinebook.dto.request;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ProjectionRequest(
    Instant datetime, BigDecimal seatPrice, UUID movieId, UUID roomId) {}
