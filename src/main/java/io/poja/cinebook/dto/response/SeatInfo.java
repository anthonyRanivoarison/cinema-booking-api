package io.poja.cinebook.dto.response;

import java.util.UUID;
import lombok.Builder;

@Builder
public record SeatInfo(UUID id, String number) {}
