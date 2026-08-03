package io.poja.cinebook.service;

import io.poja.cinebook.dto.request.MovieRequest;
import io.poja.cinebook.entity.Movie;
import io.poja.cinebook.mapper.MovieMapper;
import io.poja.cinebook.repository.MovieRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MovieService {
  private final MovieMapper mapper;
  private final MovieRepository repository;

  public Movie getById(UUID id) {
    return mapper.toModel(
        repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Movie not found")));
  }

  public List<Movie> getAll() {
    return mapper.toModel(repository.findAll());
  }

  public Movie create(MovieRequest request) {
    Movie movie =
        Movie.builder()
            .id(UUID.randomUUID())
            .title(request.title())
            .gender(request.gender())
            .description(request.description())
            .duration(request.duration())
            .build();
    return mapper.toModel(repository.save(mapper.toEntity(movie)));
  }

  public Movie update(MovieRequest request, UUID id) {
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
            .build();
    return mapper.toModel(repository.save(mapper.toEntity(movie)));
  }

  public void delete(UUID id) {
    if (!repository.existsById(id)) {
      throw new EntityNotFoundException(String.format("Movie to delete with ID %s not found", id));
    }
    repository.deleteById(id);
  }
}
