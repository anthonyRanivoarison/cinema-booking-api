package io.poja.cinebook.mapper;

import io.poja.cinebook.dto.response.MovieSummary;
import io.poja.cinebook.dto.response.ProjectionSummary;
import io.poja.cinebook.dto.response.ReservationResponse;
import io.poja.cinebook.dto.response.RoomSummary;
import io.poja.cinebook.dto.response.SeatInfo;
import io.poja.cinebook.dto.response.UserSummary;
import io.poja.cinebook.entity.Reservation;
import io.poja.cinebook.repository.model.JReservation;
import io.poja.cinebook.repository.model.JSeat;
import io.poja.cinebook.service.ProjectionService;
import io.poja.cinebook.service.SeatService;
import io.poja.cinebook.service.UserService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ReservationMapper {
  private final UserService userService;
  private final ProjectionService projectionService;
  private final SeatService seatService;
  private final UserMapper userMapper;
  private final ProjectionMapper projectionMapper;
  private final SeatMapper seatMapper;

  public Reservation toModel(JReservation entity) {
    return Reservation.builder()
        .id(entity.getId())
        .createdAt(entity.getCreatedAt())
        .userId(entity.getUser().getId())
        .projectionId(entity.getProjection().getId())
        .seatIds(entity.getSeats().stream().map(JSeat::getId).toList())
        .status(entity.getStatus())
        .ticketUrl(entity.getTicketUrl())
        .idempotencyKey(
            entity.getIdempotencyKey() != null ? UUID.fromString(entity.getIdempotencyKey()) : null)
        .build();
  }

  public List<Reservation> toModel(List<JReservation> entities) {
    return entities.stream().map(this::toModel).toList();
  }

  public ReservationResponse toResponse(JReservation entity) {
    var movie =
        MovieSummary.builder()
            .id(entity.getProjection().getMovie().getId())
            .title(entity.getProjection().getMovie().getTitle())
            .posterUrl(entity.getProjection().getMovie().getPosterUrl())
            .gender(entity.getProjection().getMovie().getGender())
            .durationSeconds(
                entity.getProjection().getMovie().getDuration() != null
                    ? entity.getProjection().getMovie().getDuration().getSeconds()
                    : null)
            .build();

    var room =
        RoomSummary.builder()
            .id(entity.getProjection().getRoom().getId())
            .number(entity.getProjection().getRoom().getNumber())
            .capacity(entity.getProjection().getRoom().getCapacity())
            .build();

    var projection =
        ProjectionSummary.builder()
            .id(entity.getProjection().getId())
            .datetime(entity.getProjection().getDatetime())
            .seatPrice(entity.getProjection().getSeatPrice())
            .movie(movie)
            .room(room)
            .build();

    var user =
        UserSummary.builder()
            .id(entity.getUser().getId())
            .firstName(entity.getUser().getFirstName())
            .email(entity.getUser().getEmail())
            .build();

    var seats =
        entity.getSeats().stream()
            .map(s -> SeatInfo.builder().id(s.getId()).number(s.getNumber()).build())
            .toList();

    return ReservationResponse.builder()
        .id(entity.getId())
        .createdAt(entity.getCreatedAt())
        .status(entity.getStatus())
        .ticketUrl(entity.getTicketUrl())
        .user(user)
        .projection(projection)
        .seats(seats)
        .build();
  }

  public List<ReservationResponse> toResponse(List<JReservation> entities) {
    return entities.stream().map(this::toResponse).toList();
  }

  public JReservation toEntity(Reservation model) {
    var user = userService.getById(model.userId());
    var projection = projectionService.getById(model.projectionId());
    var seats =
        model.seatIds().stream().map(seatService::getById).map(seatMapper::toEntity).toList();
    return JReservation.builder()
        .id(model.id())
        .createdAt(model.createdAt())
        .user(userMapper.toEntity(user))
        .projection(projectionMapper.toEntity(projection))
        .seats(seats)
        .status(model.status())
        .ticketUrl(model.ticketUrl())
        .idempotencyKey(model.idempotencyKey() != null ? model.idempotencyKey().toString() : null)
        .build();
  }

  public List<JReservation> toEntity(List<Reservation> models) {
    return models.stream().map(this::toEntity).toList();
  }
}
