package io.poja.cinebook.dto.response;

import java.util.UUID;
import lombok.Builder;

@Builder
public record SeatAvailability(UUID seatId, String number, boolean available) {}
