package io.poja.cinebook.endpoint.rest.controller.room;

import io.poja.cinebook.dto.request.CreateRoomRequest;
import io.poja.cinebook.dto.response.RoomResponse;
import io.poja.cinebook.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
@Tag(name = "Rooms", description = "Cinema room management with auto-generated seats")
public class RoomController {
  private final RoomService service;

  @GetMapping
  @Operation(summary = "List rooms", description = "Return all rooms with capacity info.")
  @ApiResponse(responseCode = "200", description = "Room list")
  public List<RoomResponse> getAll() {
    return service.getAll();
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get room by ID", description = "Return room with its seats list.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Room found"),
    @ApiResponse(responseCode = "404", description = "Room not found"),
  })
  public RoomResponse getById(@PathVariable UUID id) {
    return service.getByIdWithDetails(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = "Create room",
      description =
          "Create a new room. Seats are automatically generated based on rows and seatsPerRow.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Room created with seats"),
    @ApiResponse(responseCode = "400", description = "Validation error"),
  })
  public RoomResponse create(@RequestBody @Valid CreateRoomRequest request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update room")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Room updated"),
    @ApiResponse(responseCode = "404", description = "Room not found"),
    @ApiResponse(responseCode = "400", description = "Validation error"),
  })
  public RoomResponse update(@PathVariable UUID id, @RequestBody @Valid CreateRoomRequest request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete room")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Room deleted"),
    @ApiResponse(responseCode = "404", description = "Room not found"),
  })
  public void delete(@PathVariable UUID id) {
    service.delete(id);
  }
}
