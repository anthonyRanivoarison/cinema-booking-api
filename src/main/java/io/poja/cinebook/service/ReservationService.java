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
}
