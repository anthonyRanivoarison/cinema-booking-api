package io.poja.cinebook.dto.response;

import io.poja.cinebook.entity.enums.UserRole;
import java.util.UUID;
import lombok.Builder;

@Builder
public record AuthResponse(
    String token, UUID userId, UserRole role, String email, String firstName, long expiresIn) {}
