package io.poja.cinebook.service;

import io.poja.cinebook.entity.Projection;
import io.poja.cinebook.mapper.ProjectionMapper;
import io.poja.cinebook.repository.ProjectionRepository;
import jakarta.persistence.EntityNotFoundException;
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
}
