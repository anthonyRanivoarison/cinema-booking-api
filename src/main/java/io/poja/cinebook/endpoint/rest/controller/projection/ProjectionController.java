package io.poja.cinebook.endpoint.rest.controller.projection;

import io.poja.cinebook.dto.request.ProjectionRequest;
import io.poja.cinebook.dto.response.ProjectionResponse;
import io.poja.cinebook.dto.response.SeatAvailability;
import io.poja.cinebook.entity.Projection;
import io.poja.cinebook.service.ProjectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projections")
@RequiredArgsConstructor
@Tag(name = "Projections", description = "Screening schedule and seat availability")
public class ProjectionController {
  private final ProjectionService service;

  @GetMapping
  @Operation(
      summary = "List projections",
      description =
          "Return all projections with nested movie and room details plus available seats count.")
  @ApiResponse(responseCode = "200", description = "Projection list")
  public List<ProjectionResponse> getAll() {
    return service.getAll();
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get projection by ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Projection found"),
    @ApiResponse(responseCode = "404", description = "Projection not found"),
  })
  public Projection getById(@PathVariable UUID id) {
    return service.getById(id);
  }

  @GetMapping("/{id}/seats")
  @Operation(
      summary = "Get seat availability",
      description = "Return all seats for a projection with their availability status.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Seat availability list"),
    @ApiResponse(responseCode = "404", description = "Projection not found"),
  })
  public List<SeatAvailability> getSeats(@PathVariable UUID id) {
    return service.getSeats(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = "Create projection",
      description = "Schedule a new projection for a movie in a room.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Projection created"),
    @ApiResponse(responseCode = "404", description = "Movie or room not found"),
  })
  public Projection create(@RequestBody ProjectionRequest request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update projection")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Projection updated"),
    @ApiResponse(responseCode = "404", description = "Projection not found"),
  })
  public Projection update(@RequestBody ProjectionRequest request, @PathVariable UUID id) {
    return service.update(request, id);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete projection")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Projection deleted"),
    @ApiResponse(responseCode = "404", description = "Projection not found"),
  })
  public void delete(@PathVariable UUID id) {
    service.delete(id);
  }
}
