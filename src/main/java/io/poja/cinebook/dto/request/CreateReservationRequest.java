package io.poja.cinebook.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CreateReservationRequest(
    @NotNull(message = "projectionId is required") UUID projectionId,
    @NotEmpty(message = "seatIds is required")
        @Size(max = 8, message = "Maximum 8 seats per reservation")
        List<UUID> seatIds,
    @NotNull(message = "idempotencyKey is required") UUID idempotencyKey) {}
