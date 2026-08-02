package io.poja.cinebook.service;

import io.poja.cinebook.entity.Seat;
import io.poja.cinebook.mapper.SeatMapper;
import io.poja.cinebook.repository.SeatRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SeatService {
  private final SeatMapper mapper;
  private final SeatRepository repository;

  public Seat getById(UUID id) {
    return mapper.toModel(
        repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Seat not found")));
  }
}
