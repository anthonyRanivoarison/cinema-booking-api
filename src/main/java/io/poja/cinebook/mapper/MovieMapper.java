package io.poja.cinebook.mapper;

import io.poja.cinebook.entity.Movie;
import io.poja.cinebook.repository.model.JMovie;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MovieMapper {
  public Movie toModel(JMovie entity) {
    return Movie.builder()
        .id(entity.getId())
        .title(entity.getTitle())
        .gender(entity.getGender())
        .description(entity.getDescription())
        .duration(entity.getDuration())
        .posterUrl(entity.getPosterUrl())
        .trailerYoutubeKey(entity.getTrailerYoutubeKey())
        .tmdbId(entity.getTmdbId())
        .build();
  }

  public List<Movie> toModel(List<JMovie> entities) {
    return entities.stream().map(this::toModel).toList();
  }

  public JMovie toEntity(Movie model) {
    return JMovie.builder()
        .id(model.id())
        .title(model.title())
        .gender(model.gender())
        .description(model.description())
        .duration(model.duration())
        .posterUrl(model.posterUrl())
        .trailerYoutubeKey(model.trailerYoutubeKey())
        .tmdbId(model.tmdbId())
        .build();
  }

  public List<JMovie> toEntity(List<Movie> models) {
    return models.stream().map(this::toEntity).toList();
  }
}
