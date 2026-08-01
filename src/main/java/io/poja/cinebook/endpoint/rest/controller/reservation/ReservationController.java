package io.poja.cinebook.endpoint.rest.controller.reservation;

import io.poja.cinebook.entity.Reservation;
import io.poja.cinebook.service.ReservationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
