package io.poja.cinebook.endpoint.rest.controller.reservation;

import io.poja.cinebook.entity.Reservation;
import io.poja.cinebook.service.ReservationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {
  private final ReservationService service;

  @GetMapping
  public List<Reservation> getAll() {
    return service.getAll();
  }

  @GetMapping("/{id}")
  public Reservation getById(@PathVariable UUID id) {
    return service.getById(id);
  }

  @PutMapping("/{id}")
  public Reservation update(@RequestBody Reservation reservation, @PathVariable UUID id) {
    return service.update(reservation, id);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    service.delete(id);
  }
}
