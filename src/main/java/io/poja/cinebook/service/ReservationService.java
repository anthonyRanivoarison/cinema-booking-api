package io.poja.cinebook.service;

import io.poja.cinebook.dto.request.CreateReservationRequest;
import io.poja.cinebook.endpoint.event.EventProducer;
import io.poja.cinebook.endpoint.event.model.SendEmailRequested;
import io.poja.cinebook.entity.Reservation;
import io.poja.cinebook.entity.enums.ReservationStatus;
import io.poja.cinebook.entity.enums.UserRole;
import io.poja.cinebook.exception.ApiException;
import io.poja.cinebook.exception.ForbiddenException;
import io.poja.cinebook.mapper.ReservationMapper;
import io.poja.cinebook.repository.ProjectionRepository;
import io.poja.cinebook.repository.ReservationRepository;
import io.poja.cinebook.repository.SeatRepository;
import io.poja.cinebook.repository.UserRepository;
import io.poja.cinebook.repository.model.JMovie;
import io.poja.cinebook.repository.model.JProjection;
import io.poja.cinebook.repository.model.JReservation;
import io.poja.cinebook.repository.model.JRoom;
import io.poja.cinebook.repository.model.JSeat;
import io.poja.cinebook.repository.model.JUser;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ReservationService {
  private final ReservationMapper mapper;
  private final ReservationRepository repository;
  private final ProjectionRepository projectionRepository;
  private final UserRepository userRepository;
  private final SeatRepository seatRepository;
  private final EventProducer<SendEmailRequested> eventProducer;
  private final TicketService ticketService;

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

  public List<Reservation> getByUserId(String userId) {
    return mapper.toModel(repository.findByUserId(UUID.fromString(userId)));
  }

  public Reservation create(CreateReservationRequest request) {
    JProjection projection =
        projectionRepository
            .findById(request.projectionId())
            .orElseThrow(() -> new EntityNotFoundException("Projection not found"));
    List<UUID> takenSeatIds =
        repository.findTakenOrPendingSeatIdsByProjectionId(request.projectionId());
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
            .status(ReservationStatus.PENDING)
            .build();
    return mapper.toModel(repository.save(mapper.toEntity(reservation)));
  }

  @Transactional
  public Reservation approve(UUID id) {
    JReservation jReservation =
        repository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Reservation not found"));
    if (jReservation.getStatus() != ReservationStatus.PENDING) {
      throw new ApiException(
          String.format("Reservation is already %s", jReservation.getStatus()),
          HttpStatus.CONFLICT);
    }

    JProjection projection = jReservation.getProjection();
    JRoom room = projection.getRoom();
    JMovie movie = projection.getMovie();
    JUser user = jReservation.getUser();
    List<JSeat> seats = jReservation.getSeats();

    JSeat primarySeat = seats.isEmpty() ? null : seats.get(0);

    String ticketUrl =
        ticketService.generateAndUpload(
            mapper.toModel(jReservation), projection, primarySeat, room, user, movie);

    jReservation.setStatus(ReservationStatus.APPROVED);
    jReservation.setTicketUrl(ticketUrl);
    JReservation saved = repository.save(jReservation);

    String userEmail = user.getEmail();
    String movieTitle = movie != null ? movie.getTitle() : "Unknown";
    eventProducer.accept(
        List.of(
            SendEmailRequested.builder()
                .to(userEmail)
                .subject("Your ticket is ready!")
                .htmlBody(
                    """
<html>
  <body>
    <p>Dear %s,</p>
    <p>Your reservation for the movie "<strong>%s</strong>" has been <strong>approved</strong>.</p>
    <p>Your ticket is ready. Download it from: <a href="%s">here</a></p>
    <p>Please present this ticket at the entrance.</p>
    <p>Best regards,<br>The Cinema Booking Team</p>
  </body>
</html>
"""
                        .formatted(userEmail, movieTitle, ticketUrl))
                .build()));

    return mapper.toModel(saved);
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
