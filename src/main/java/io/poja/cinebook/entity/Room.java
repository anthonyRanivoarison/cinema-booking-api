package io.poja.cinebook.entity;

import java.util.UUID;
import lombok.Builder;

@Builder
public record Room(UUID id, String number, int capacity) {}
