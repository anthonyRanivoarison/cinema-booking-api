package io.poja.cinebook.mapper;

import io.poja.cinebook.entity.Reservation;
import io.poja.cinebook.repository.model.JReservation;
import io.poja.cinebook.repository.model.JSeat;
import io.poja.cinebook.service.ProjectionService;
import io.poja.cinebook.service.SeatService;
import io.poja.cinebook.service.UserService;
import java.util.List;
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
        .build();
  }

  public List<Reservation> toModel(List<JReservation> entities) {
    return entities.stream().map(this::toModel).toList();
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
        .build();
  }

  public List<JReservation> toEntity(List<Reservation> models) {
    return models.stream().map(this::toEntity).toList();
  }
}
