package io.poja.cinebook.service;

import io.poja.cinebook.entity.Movie;
import io.poja.cinebook.mapper.MovieMapper;
import io.poja.cinebook.repository.MovieRepository;
import jakarta.persistence.EntityNotFoundException;
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
}
