package io.poja.cinebook.service;

import io.poja.cinebook.dto.request.CreateReservationRequest;
import io.poja.cinebook.entity.Reservation;
import io.poja.cinebook.entity.enums.UserRole;
import io.poja.cinebook.exception.ApiException;
import io.poja.cinebook.exception.ForbiddenException;
import io.poja.cinebook.mapper.ReservationMapper;
import io.poja.cinebook.repository.ProjectionRepository;
import io.poja.cinebook.repository.ReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ReservationService {
  private final ReservationMapper mapper;
  private final ReservationRepository repository;
  private final ProjectionRepository projectionRepository;

  public Reservation getById(UUID id, String currentUserId, String role) {
    Reservation reservation =
        mapper.toModel(
            repository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found")));
    if (UserRole.CLIENT.name().equals(role)
        && !reservation.userId().equals(UUID.fromString(currentUserId))) {
      throw new ForbiddenException("Forbidden: a CLIENT can only access their own reservation");
    }
    return reservation;
  }

  public List<Reservation> getAll() {
    return mapper.toModel(repository.findAll());
  }

  public Reservation create(CreateReservationRequest request) {
    if (!projectionRepository.existsById(request.projectionId())) {
      throw new EntityNotFoundException("Projection not found");
    }
    List<UUID> takenSeatIds = repository.findTakenSeatIdsByProjectionId(request.projectionId());
    List<UUID> conflicts = request.seatIds().stream().filter(takenSeatIds::contains).toList();
    if (!conflicts.isEmpty()) {
      throw new ApiException(
          String.format("Seat(s) already reserved: %s", conflicts), HttpStatus.CONFLICT);
    }
    Reservation reservation =
        Reservation.builder()
            .id(UUID.randomUUID())
            .createdAt(Instant.now())
            .userId(request.userId())
            .projectionId(request.projectionId())
            .seatIds(request.seatIds())
            .build();
    return mapper.toModel(repository.save(mapper.toEntity(reservation)));
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
