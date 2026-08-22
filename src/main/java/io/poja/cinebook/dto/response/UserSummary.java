package io.poja.cinebook.dto.response;

import java.util.UUID;
import lombok.Builder;

@Builder
public record UserSummary(UUID id, String firstName, String email) {}
