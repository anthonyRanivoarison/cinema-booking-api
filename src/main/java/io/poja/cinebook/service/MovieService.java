package io.poja.cinebook.service;

import io.poja.cinebook.client.TmdbClient;
import io.poja.cinebook.client.TmdbMovie;
import io.poja.cinebook.client.TmdbSearchResult;
import io.poja.cinebook.dto.request.MovieRequest;
import io.poja.cinebook.entity.Movie;
import io.poja.cinebook.entity.enums.MovieGender;
import io.poja.cinebook.exception.ApiException;
import io.poja.cinebook.mapper.MovieMapper;
import io.poja.cinebook.repository.MovieRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MovieService {
  private static final Map<String, MovieGender> TMDB_GENRE_BY_NAME =
      Map.of(
          "Action", MovieGender.ACTION,
          "Adventure", MovieGender.ACTION,
          "Animation", MovieGender.ANIMATION,
          "Comedy", MovieGender.COMEDY,
          "Drama", MovieGender.DRAMA,
          "Romance", MovieGender.ROMANCE,
          "Science Fiction", MovieGender.SCIFI,
          "Fantasy", MovieGender.FANTASY,
          "Thriller", MovieGender.THRILLER);

  private final MovieMapper mapper;
  private final MovieRepository repository;
  private final TmdbClient tmdbClient;

  public Movie getById(UUID id) {
    return mapper.toModel(
        repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Movie not found")));
  }

  public List<Movie> getAll() {
    return mapper.toModel(repository.findAll());
  }

  public Movie create(MovieRequest request) {
    assertValid(request);
    Movie movie =
        Movie.builder()
            .id(UUID.randomUUID())
            .title(request.title())
            .gender(request.gender())
            .description(request.description())
            .duration(request.duration())
            .posterUrl(request.posterUrl())
            .trailerYoutubeKey(request.trailerYoutubeKey())
            .tmdbId(request.tmdbId())
            .build();
    return mapper.toModel(repository.save(mapper.toEntity(movie)));
  }

  public Movie update(MovieRequest request, UUID id) {
    assertValid(request);
    if (repository.findById(id).isEmpty()) {
      throw new EntityNotFoundException(String.format("Movie with ID %s not found", id));
    }
    Movie movie =
        Movie.builder()
            .id(id)
            .title(request.title())
            .gender(request.gender())
            .description(request.description())
            .duration(request.duration())
            .posterUrl(request.posterUrl())
            .trailerYoutubeKey(request.trailerYoutubeKey())
            .tmdbId(request.tmdbId())
            .build();
    return mapper.toModel(repository.save(mapper.toEntity(movie)));
  }

  public Movie importFromTmdb(int tmdbId) {
    repository
        .findByTmdbId((long) tmdbId)
        .ifPresent(
            existing -> {
              throw new ApiException(
                  String.format("Movie with TMDB id %s is already imported", tmdbId),
                  HttpStatus.CONFLICT);
            });
    TmdbMovie tmdb = tmdbClient.getMovie(tmdbId);
    Movie movie =
        Movie.builder()
            .id(UUID.randomUUID())
            .title(tmdb.title())
            .gender(mapGender(tmdb))
            .description(tmdb.overview())
            .duration(Duration.ofMinutes(tmdb.runtime()))
            .posterUrl(TmdbClient.composePosterUrl(tmdb.posterPath()))
            .trailerYoutubeKey(tmdbClient.getTrailerYoutubeKey(tmdbId))
            .tmdbId((long) tmdbId)
            .build();
    return mapper.toModel(repository.save(mapper.toEntity(movie)));
  }

  public List<TmdbSearchResult> searchTmdb(String query) {
    return tmdbClient.search(query);
  }

  private MovieGender mapGender(TmdbMovie tmdb) {
    if (tmdb.genres() == null) {
      return null;
    }
    return tmdb.genres().stream()
        .map(genre -> TMDB_GENRE_BY_NAME.get(genre.name()))
        .filter(java.util.Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  private void assertValid(MovieRequest request) {
    if (request.duration().isZero() || request.duration().isNegative()) {
      throw new ApiException("duration must be positive", HttpStatus.BAD_REQUEST);
    }
  }

  public void delete(UUID id) {
    if (!repository.existsById(id)) {
      throw new EntityNotFoundException(String.format("Movie to delete with ID %s not found", id));
    }
    repository.deleteById(id);
  }
}
