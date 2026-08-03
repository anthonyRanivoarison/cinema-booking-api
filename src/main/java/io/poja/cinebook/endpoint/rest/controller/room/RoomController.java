package io.poja.cinebook.endpoint.rest.controller.room;

import io.poja.cinebook.entity.Room;
import io.poja.cinebook.service.RoomService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {
  private final RoomService service;

  @GetMapping
  public List<Room> getAll() {
    return service.getAll();
  }
}
