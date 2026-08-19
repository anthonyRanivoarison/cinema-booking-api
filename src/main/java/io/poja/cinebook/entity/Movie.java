package io.poja.cinebook.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import io.poja.cinebook.entity.enums.MovieGender;
import java.time.Duration;
import java.util.UUID;
import lombok.Builder;

@Builder
public record Movie(
    UUID id,
    String title,
    MovieGender gender,
    String description,
    Duration duration,
    String posterUrl,
    String trailerYoutubeKey,
    Long tmdbId) {

  @JsonGetter("durationSeconds")
  public Long durationSeconds() {
    return duration != null ? duration.getSeconds() : null;
  }
}
