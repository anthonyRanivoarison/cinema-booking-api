package io.poja.cinebook.dto.request;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record SignUpRequest(
    String firstName,
    String lastName,
    LocalDate birthDate,
    String email,
    String password,
    String phone) {}
