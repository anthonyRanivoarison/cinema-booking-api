package io.poja.cinebook.endpoint.rest.controller.reservation;

import io.poja.cinebook.dto.request.CreateReservationRequest;
import io.poja.cinebook.entity.Reservation;
import io.poja.cinebook.service.ReservationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

  @GetMapping("/me")
  public List<Reservation> getMe(@AuthenticationPrincipal Jwt jwt) {
    return service.getByUserId(jwt.getSubject());
  }

  @GetMapping("/{id}")
  public Reservation getById(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
    return service.getById(id, jwt.getSubject(), jwt.getClaimAsString("role"));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Reservation create(@RequestBody @Valid CreateReservationRequest request) {
    return service.create(request);
  }

  @PatchMapping("/{id}/approve")
  public Reservation approve(@PathVariable UUID id) {
    return service.approve(id);
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
