package io.poja.cinebook.mapper;

import io.poja.cinebook.entity.Seat;
import io.poja.cinebook.repository.model.JSeat;
import io.poja.cinebook.service.RoomService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SeatMapper {
  private final RoomService roomService;
  private final RoomMapper roomMapper;

  public Seat toModel(JSeat entity) {
    return Seat.builder()
        .id(entity.getId())
        .number(entity.getNumber())
        .roomId(entity.getRoom().getId())
        .build();
  }

  public List<Seat> toModel(List<JSeat> entities) {
    return entities.stream().map(this::toModel).toList();
  }

  public JSeat toEntity(Seat model) {
    var room = roomService.getById(model.roomId());
    return JSeat.builder()
        .id(model.id())
        .number(model.number())
        .room(roomMapper.toEntity(room))
        .build();
  }

  public List<JSeat> toEntity(List<Seat> models) {
    return models.stream().map(this::toEntity).toList();
  }
}
