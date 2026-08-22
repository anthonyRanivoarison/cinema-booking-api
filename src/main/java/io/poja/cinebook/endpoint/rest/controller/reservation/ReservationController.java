package io.poja.cinebook.endpoint.rest.controller.reservation;

import io.poja.cinebook.dto.request.CreateReservationRequest;
import io.poja.cinebook.dto.response.ReservationResponse;
import io.poja.cinebook.entity.Reservation;
import io.poja.cinebook.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Reservations", description = "Seat reservation management")
public class ReservationController {
  private final ReservationService service;

  @GetMapping
  @Operation(
      summary = "List all reservations",
      description =
          "Return all reservations with nested user, projection, and seat details. EMPLOYEE/ADMIN"
              + " only.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Reservation list"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
  })
  public List<ReservationResponse> getAll() {
    return service.getAll();
  }

  @GetMapping("/me")
  @Operation(
      summary = "My reservations",
      description = "Return reservations for the authenticated user.")
  @ApiResponse(responseCode = "200", description = "User's reservation list")
  public List<ReservationResponse> getMe(@AuthenticationPrincipal Jwt jwt) {
    return service.getByUserId(jwt.getSubject());
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get reservation by ID",
      description = "CLIENT can only access their own reservations.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Reservation found"),
    @ApiResponse(responseCode = "403", description = "Not the reservation owner"),
    @ApiResponse(responseCode = "404", description = "Reservation not found"),
  })
  public ReservationResponse getById(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
    return service.getById(id, jwt.getSubject(), jwt.getClaimAsString("role"));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = "Create reservation",
      description = "Reserve seats for a projection. Seats must be available.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Reservation created"),
    @ApiResponse(responseCode = "404", description = "Projection not found"),
    @ApiResponse(responseCode = "409", description = "One or more seats already taken"),
  })
  public ReservationResponse create(@RequestBody @Valid CreateReservationRequest request) {
    return service.create(request);
  }

  @PatchMapping("/{id}/approve")
  @Operation(
      summary = "Approve reservation",
      description =
          "Approve a pending reservation. Generates a ticket PDF, uploads it to S3, sends a"
              + " confirmation email, and returns a presigned ticket URL. EMPLOYEE/ADMIN only.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Reservation approved"),
    @ApiResponse(responseCode = "404", description = "Reservation not found"),
  })
  public ReservationResponse approve(@PathVariable UUID id) {
    return service.approve(id);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update reservation")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Reservation updated"),
    @ApiResponse(responseCode = "404", description = "Reservation not found"),
  })
  public ReservationResponse update(@RequestBody Reservation reservation, @PathVariable UUID id) {
    return service.update(reservation, id);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete reservation")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Reservation deleted"),
    @ApiResponse(responseCode = "404", description = "Reservation not found"),
  })
  public void delete(@PathVariable UUID id) {
    service.delete(id);
  }
}
