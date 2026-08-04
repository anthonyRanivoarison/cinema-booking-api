package io.poja.cinebook.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CreateReservationRequest(
    @NotNull(message = "userId is required") UUID userId,
    @NotNull(message = "projectionId is required") UUID projectionId,
    @NotNull(message = "seatIds is required") List<UUID> seatIds) {}
