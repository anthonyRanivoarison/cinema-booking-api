package io.poja.cinebook.service;

import io.poja.cinebook.entity.Room;
import io.poja.cinebook.mapper.RoomMapper;
import io.poja.cinebook.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RoomService {
  private final RoomMapper mapper;
  private final RoomRepository repository;

  public Room getById(UUID id) {
    return mapper.toModel(
        repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Room not found")));
  }

  public List<Room> getAll() {
    return mapper.toModel(repository.findAll());
  }
}
