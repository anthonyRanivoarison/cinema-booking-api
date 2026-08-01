package io.poja.cinebook.entity;

import io.poja.cinebook.entity.enums.MovieGender;
import java.time.Duration;
import java.util.UUID;
import lombok.Builder;

@Builder
public record Movie(
    UUID id, String title, MovieGender gender, String description, Duration duration) {}
