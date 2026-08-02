package io.poja.cinebook.dto.request;

import io.poja.cinebook.entity.enums.UserRole;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record CreateUserRequest(
    String firstName,
    String lastName,
    LocalDate birthDate,
    String email,
    String password,
    String phone,
    UserRole role) {}
