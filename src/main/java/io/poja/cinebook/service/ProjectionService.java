package io.poja.cinebook.service;

import io.poja.cinebook.dto.request.ProjectionRequest;
import io.poja.cinebook.entity.Projection;
import io.poja.cinebook.mapper.ProjectionMapper;
import io.poja.cinebook.repository.ProjectionRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProjectionService {
  private final ProjectionMapper mapper;
  private final ProjectionRepository repository;

  public Projection getById(UUID id) {
    return mapper.toModel(
        repository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Projection not found")));
  }

  public List<Projection> getAll() {
    return mapper.toModel(repository.findAll());
  }

  public Projection create(ProjectionRequest request) {
    Projection projection =
        Projection.builder()
            .id(UUID.randomUUID())
            .datetime(request.datetime())
            .seatPrice(request.seatPrice())
            .movieId(request.movieId())
            .roomId(request.roomId())
            .build();
    return mapper.toModel(repository.save(mapper.toEntity(projection)));
  }

  public Projection update(ProjectionRequest request, UUID id) {
    if (repository.findById(id).isEmpty()) {
      throw new EntityNotFoundException(String.format("Projection with ID %s not found", id));
    }
    Projection projection =
        Projection.builder()
            .id(id)
            .datetime(request.datetime())
            .seatPrice(request.seatPrice())
            .movieId(request.movieId())
            .roomId(request.roomId())
            .build();
    return mapper.toModel(repository.save(mapper.toEntity(projection)));
  }

  public void delete(UUID id) {
    repository.deleteById(id);
  }
}
