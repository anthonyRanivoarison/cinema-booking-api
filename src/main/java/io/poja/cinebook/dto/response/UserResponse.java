package io.poja.cinebook.dto.response;

import io.poja.cinebook.entity.enums.UserRole;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;

@Builder
public record UserResponse(
    UUID id,
    String firstName,
    String lastName,
    LocalDate birthDate,
    String email,
    String phone,
    UserRole role) {}
