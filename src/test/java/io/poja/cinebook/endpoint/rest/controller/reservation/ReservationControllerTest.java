package io.poja.cinebook.endpoint.rest.controller.reservation;

import static io.poja.cinebook.entity.enums.UserRole.ADMIN;
import static io.poja.cinebook.entity.enums.UserRole.CLIENT;
import static io.poja.cinebook.entity.enums.UserRole.EMPLOYEE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.poja.cinebook.config.JwtConfig;
import io.poja.cinebook.config.JwtTokenProvider;
import io.poja.cinebook.config.SecurityConfig;
import io.poja.cinebook.dto.request.CreateReservationRequest;
import io.poja.cinebook.dto.response.ProjectionSummary;
import io.poja.cinebook.dto.response.ReservationResponse;
import io.poja.cinebook.dto.response.UserSummary;
import io.poja.cinebook.entity.Reservation;
import io.poja.cinebook.entity.User;
import io.poja.cinebook.entity.enums.UserRole;
import io.poja.cinebook.exception.ForbiddenException;
import io.poja.cinebook.exception.GlobalExceptionHandler;
import io.poja.cinebook.service.ReservationService;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ReservationController.class)
@Import({
  SecurityConfig.class,
  JwtConfig.class,
  JwtTokenProvider.class,
  GlobalExceptionHandler.class
})
class ReservationControllerTest {

  private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID PROJECTION_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final UUID SEAT_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
  private static final Instant CREATED_AT = Instant.parse("2026-08-05T10:00:00Z");

  @Autowired private MockMvc mockMvc;
  @Autowired private JwtTokenProvider tokenProvider;
  @Autowired private ObjectMapper objectMapper;

  @org.springframework.boot.test.mock.mockito.MockBean private ReservationService service;

  @Test
  void getAll_returnsReservations() throws Exception {
    when(service.getAll()).thenReturn(List.of(response()));

    mockMvc
        .perform(get("/reservations").header("Authorization", bearer(ADMIN)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(ID.toString()))
        .andExpect(jsonPath("$[0].user.id").value(USER_ID.toString()))
        .andExpect(jsonPath("$[0].projection.id").value(PROJECTION_ID.toString()))
        .andExpect(jsonPath("$[0].seats[0].id").value(SEAT_ID.toString()));

    verify(service).getAll();
  }

  @Test
  void getMe_returnsCurrentUserReservations() throws Exception {
    when(service.getByUserId(USER_ID.toString())).thenReturn(List.of(response()));

    mockMvc
        .perform(get("/reservations/me").header("Authorization", bearer(CLIENT)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(ID.toString()))
        .andExpect(jsonPath("$[0].user.id").value(USER_ID.toString()));

    verify(service).getByUserId(USER_ID.toString());
  }

  @Test
  void getMe_returnsUnauthorized_whenNoToken() throws Exception {
    mockMvc.perform(get("/reservations/me")).andExpect(status().isUnauthorized());
    verify(service, never()).getByUserId(anyString());
  }

  @Test
  void getById_returnsReservation() throws Exception {
    when(service.getById(eq(ID), anyString(), anyString())).thenReturn(response());

    mockMvc
        .perform(get("/reservations/{id}", ID).header("Authorization", bearer(ADMIN)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(ID.toString()))
        .andExpect(jsonPath("$.user.id").value(USER_ID.toString()));

    verify(service).getById(eq(ID), eq(USER_ID.toString()), eq(ADMIN.name()));
  }

  @Test
  void create_persistsAndReturnsCreatedReservation() throws Exception {
    when(service.create(any(CreateReservationRequest.class))).thenReturn(response());

    mockMvc
        .perform(
            post("/reservations")
                .header("Authorization", bearer(CLIENT))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequestBody()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(ID.toString()))
        .andExpect(jsonPath("$.projection.id").value(PROJECTION_ID.toString()));

    ArgumentCaptor<CreateReservationRequest> captor =
        ArgumentCaptor.forClass(CreateReservationRequest.class);
    verify(service).create(captor.capture());
    CreateReservationRequest request = captor.getValue();
    org.assertj.core.api.Assertions.assertThat(request.userId()).isEqualTo(USER_ID);
    org.assertj.core.api.Assertions.assertThat(request.projectionId()).isEqualTo(PROJECTION_ID);
    org.assertj.core.api.Assertions.assertThat(request.seatIds()).containsExactly(SEAT_ID);
  }

  @Test
  void update_savesAndReturnsReservation() throws Exception {
    when(service.update(any(Reservation.class), eq(ID))).thenReturn(response());

    mockMvc
        .perform(
            put("/reservations/{id}", ID)
                .header("Authorization", bearer(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reservationBody()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(ID.toString()));

    verify(service).update(any(Reservation.class), eq(ID));
  }

  @Test
  void delete_removesReservation() throws Exception {
    mockMvc
        .perform(delete("/reservations/{id}", ID).header("Authorization", bearer(ADMIN)))
        .andExpect(status().isNoContent());

    verify(service).delete(ID);
  }

  @Test
  void getById_returnsNotFound_whenMissing() throws Exception {
    when(service.getById(eq(ID), anyString(), anyString()))
        .thenThrow(new EntityNotFoundException("Reservation not found"));

    mockMvc
        .perform(get("/reservations/{id}", ID).header("Authorization", bearer(ADMIN)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Reservation not found"))
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void getById_returnsForbidden_whenClientNotOwner() throws Exception {
    when(service.getById(eq(ID), anyString(), eq(CLIENT.name())))
        .thenThrow(
            new ForbiddenException("Forbidden: a CLIENT can only access their own reservation"));

    mockMvc
        .perform(get("/reservations/{id}", ID).header("Authorization", bearer(CLIENT)))
        .andExpect(status().isForbidden())
        .andExpect(
            jsonPath("$.message")
                .value("Forbidden: a CLIENT can only access their own reservation"));
  }

  @Test
  void create_returnsBadRequest_whenBodyMalformed() throws Exception {
    mockMvc
        .perform(
            post("/reservations")
                .header("Authorization", bearer(CLIENT))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{not json"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void create_returnsBadRequest_whenValidationFails() throws Exception {
    mockMvc
        .perform(
            post("/reservations")
                .header("Authorization", bearer(CLIENT))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"projectionId\":\"" + PROJECTION_ID + "\"}"))
        .andExpect(status().isBadRequest());
    verify(service, never()).create(any());
  }

  @Test
  void getById_returnsBadRequest_whenIdNotUuid() throws Exception {
    mockMvc
        .perform(get("/reservations/{id}", "abc").header("Authorization", bearer(ADMIN)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getAll_returnsUnauthorized_whenNoToken() throws Exception {
    mockMvc.perform(get("/reservations")).andExpect(status().isUnauthorized());
    verify(service, never()).getAll();
  }

  @Test
  void getAll_returnsForbidden_forClientRole() throws Exception {
    mockMvc
        .perform(get("/reservations").header("Authorization", bearer(CLIENT)))
        .andExpect(status().isForbidden());
    verify(service, never()).getAll();
  }

  @Test
  void getAll_returnsOk_forEmployeeRole() throws Exception {
    when(service.getAll()).thenReturn(List.of());

    mockMvc
        .perform(get("/reservations").header("Authorization", bearer(EMPLOYEE)))
        .andExpect(status().isOk());
  }

  @Test
  void create_returnsCreated_forClientRole() throws Exception {
    when(service.create(any(CreateReservationRequest.class))).thenReturn(response());

    mockMvc
        .perform(
            post("/reservations")
                .header("Authorization", bearer(CLIENT))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequestBody()))
        .andExpect(status().isCreated());
  }

  @Test
  void update_returnsForbidden_forClientRole() throws Exception {
    mockMvc
        .perform(
            put("/reservations/{id}", ID)
                .header("Authorization", bearer(CLIENT))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reservationBody()))
        .andExpect(status().isForbidden());
    verify(service, never()).update(any(), any());
  }

  @Test
  void update_returnsOk_forEmployeeRole() throws Exception {
    when(service.update(any(Reservation.class), eq(ID))).thenReturn(response());

    mockMvc
        .perform(
            put("/reservations/{id}", ID)
                .header("Authorization", bearer(EMPLOYEE))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reservationBody()))
        .andExpect(status().isOk());
  }

  @Test
  void delete_returnsForbidden_forClientRole() throws Exception {
    mockMvc
        .perform(delete("/reservations/{id}", ID).header("Authorization", bearer(CLIENT)))
        .andExpect(status().isForbidden());
    verify(service, never()).delete(ID);
  }

  private String createRequestBody() throws Exception {
    return objectMapper.writeValueAsString(
        Map.of(
            "userId", USER_ID.toString(),
            "projectionId", PROJECTION_ID.toString(),
            "seatIds", List.of(SEAT_ID.toString())));
  }

  private String reservationBody() throws Exception {
    return objectMapper.writeValueAsString(
        Map.of(
            "id", ID.toString(),
            "createdAt", CREATED_AT.toString(),
            "userId", USER_ID.toString(),
            "projectionId", PROJECTION_ID.toString(),
            "seatIds", List.of(SEAT_ID.toString())));
  }

  private String bearer(UserRole role) {
    return "Bearer " + token(role);
  }

  private String token(UserRole role) {
    User user = User.builder().id(USER_ID).email("user@example.com").role(role).build();
    return tokenProvider.generateToken(user);
  }

  private ReservationResponse response() {
    return ReservationResponse.builder()
        .id(ID)
        .createdAt(CREATED_AT)
        .user(UserSummary.builder().id(USER_ID).firstName("John").email("user@example.com").build())
        .projection(ProjectionSummary.builder().id(PROJECTION_ID).build())
        .seats(
            List.of(
                io.poja.cinebook.dto.response.SeatInfo.builder().id(SEAT_ID).number("A1").build()))
        .build();
  }
}
