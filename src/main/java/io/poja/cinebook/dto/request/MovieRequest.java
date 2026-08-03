package io.poja.cinebook.dto.request;

import io.poja.cinebook.entity.enums.MovieGender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import lombok.Builder;

@Builder
public record MovieRequest(
    @NotBlank(message = "title is required") String title,
    @NotNull(message = "gender is required") MovieGender gender,
    String description,
    @NotNull(message = "duration is required") Duration duration) {}
