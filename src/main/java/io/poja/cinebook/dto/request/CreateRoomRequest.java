package io.poja.cinebook.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateRoomRequest(
    @NotBlank(message = "number is required") String number,
    @NotNull(message = "capacity is required") @Min(1) int capacity,
    Integer rows,
    Integer seatsPerRow) {}
