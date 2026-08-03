package io.poja.cinebook.dto.request;

import io.poja.cinebook.entity.enums.MovieGender;
import java.time.Duration;
import lombok.Builder;

@Builder
public record MovieRequest(
    String title, MovieGender gender, String description, Duration duration) {}
