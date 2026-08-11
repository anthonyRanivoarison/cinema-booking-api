package io.poja.cinebook.endpoint.rest.controller.projection;

import io.poja.cinebook.dto.request.ProjectionRequest;
import io.poja.cinebook.dto.response.SeatAvailability;
import io.poja.cinebook.entity.Projection;
import io.poja.cinebook.service.ProjectionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projections")
@RequiredArgsConstructor
public class ProjectionController {
  private final ProjectionService service;

  @GetMapping
  public List<Projection> getAll() {
    return service.getAll();
  }

  @GetMapping("/{id}")
  public Projection getById(@PathVariable UUID id) {
    return service.getById(id);
  }

  @GetMapping("/{id}/seats")
  public List<SeatAvailability> getSeats(@PathVariable UUID id) {
    return service.getSeats(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Projection create(@RequestBody ProjectionRequest request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  public Projection update(@RequestBody ProjectionRequest request, @PathVariable UUID id) {
    return service.update(request, id);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    service.delete(id);
  }
}
