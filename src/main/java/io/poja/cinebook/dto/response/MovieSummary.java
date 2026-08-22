package io.poja.cinebook.dto.response;

import io.poja.cinebook.entity.enums.MovieGender;
import java.util.UUID;
import lombok.Builder;

@Builder
public record MovieSummary(
    UUID id, String title, String posterUrl, MovieGender gender, Long durationSeconds) {}
