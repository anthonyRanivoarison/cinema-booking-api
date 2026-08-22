package io.poja.cinebook.endpoint.rest.controller.room;

import io.poja.cinebook.dto.request.CreateRoomRequest;
import io.poja.cinebook.dto.response.RoomResponse;
import io.poja.cinebook.service.RoomService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {
  private final RoomService service;

  @GetMapping
  public List<RoomResponse> getAll() {
    return service.getAll();
  }

  @GetMapping("/{id}")
  public RoomResponse getById(@PathVariable UUID id) {
    return service.getByIdWithDetails(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public RoomResponse create(@RequestBody @Valid CreateRoomRequest request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  public RoomResponse update(@PathVariable UUID id, @RequestBody @Valid CreateRoomRequest request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    service.delete(id);
  }
}
