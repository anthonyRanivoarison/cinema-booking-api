package io.poja.cinebook.entity;

import java.util.UUID;
import lombok.Builder;

@Builder
public record Seat(UUID id, String number, UUID roomId) {}
