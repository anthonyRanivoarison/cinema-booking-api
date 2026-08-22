package io.poja.cinebook.dto.response;

import java.util.UUID;
import lombok.Builder;

@Builder
public record RoomSummary(UUID id, String number, int capacity) {}
