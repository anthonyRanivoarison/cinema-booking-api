package io.poja.cinebook.entity;

import io.poja.cinebook.entity.enums.UserRole;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;

@Builder
public record User(
    UUID id,
    String firstName,
    String lastName,
    LocalDate birthDate,
    String email,
    String password,
    String phone,
    UserRole role) {}
