package io.poja.cinebook.mapper;

import io.poja.cinebook.entity.Room;
import io.poja.cinebook.entity.Seat;
import io.poja.cinebook.repository.model.JRoom;
import io.poja.cinebook.repository.model.JSeat;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {
  public Room toModel(JRoom entity) {
    return Room.builder()
        .id(entity.getId())
        .number(entity.getNumber())
        .capacity(entity.getCapacity())
        .build();
  }

  public Room toModel(JRoom entity, List<JSeat> seats) {
    List<Seat> seatList =
        seats.stream()
            .map(
                s ->
                    Seat.builder()
                        .id(s.getId())
                        .number(s.getNumber())
                        .roomId(entity.getId())
                        .build())
            .toList();
    return Room.builder()
        .id(entity.getId())
        .number(entity.getNumber())
        .capacity(entity.getCapacity())
        .seats(seatList)
        .build();
  }

  public List<Room> toModel(List<JRoom> entities) {
    return entities.stream().map(this::toModel).toList();
  }

  public JRoom toEntity(Room model) {
    return JRoom.builder().id(model.id()).number(model.number()).capacity(model.capacity()).build();
  }

  public List<JRoom> toEntity(List<Room> models) {
    return models.stream().map(this::toEntity).toList();
  }
}
