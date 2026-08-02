package io.poja.cinebook.service;

import io.poja.cinebook.entity.Reservation;
import io.poja.cinebook.mapper.ReservationMapper;
import io.poja.cinebook.repository.ReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ReservationService {
  private final ReservationMapper mapper;
  private final ReservationRepository repository;

  public Reservation getById(UUID id) {
    return mapper.toModel(
        repository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Reservation not found")));
  }

  public List<Reservation> getAll() {
    return mapper.toModel(repository.findAll());
  }

  public Reservation update(Reservation reservation, UUID id) {
    if (repository.findById(id).isEmpty()) {
      throw new EntityNotFoundException(
          String.format("Reservation to update with ID %s not found", id));
    }
    return mapper.toModel(repository.save(mapper.toEntity(reservation)));
  }

  public void delete(UUID id) {
    if (!repository.existsById(id)) {
      throw new EntityNotFoundException(
          String.format("Reservation to delete with ID %s not found", id));
    }
    repository.deleteById(id);
  }
}
