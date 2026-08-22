package io.poja.cinebook.endpoint.rest.controller.projection;

import static io.poja.cinebook.entity.enums.UserRole.ADMIN;
import static io.poja.cinebook.entity.enums.UserRole.CLIENT;
import static io.poja.cinebook.entity.enums.UserRole.EMPLOYEE;
import static org.mockito.ArgumentMatchers.any;
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
import io.poja.cinebook.dto.request.ProjectionRequest;
import io.poja.cinebook.dto.response.MovieSummary;
import io.poja.cinebook.dto.response.ProjectionResponse;
import io.poja.cinebook.dto.response.RoomSummary;
import io.poja.cinebook.dto.response.SeatAvailability;
import io.poja.cinebook.entity.Projection;
import io.poja.cinebook.entity.User;
import io.poja.cinebook.entity.enums.UserRole;
import io.poja.cinebook.exception.GlobalExceptionHandler;
import io.poja.cinebook.service.ProjectionService;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
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
@WebMvcTest(ProjectionController.class)
@Import({
  SecurityConfig.class,
  JwtConfig.class,
  JwtTokenProvider.class,
  GlobalExceptionHandler.class
})
class ProjectionControllerTest {

  private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID MOVIE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID ROOM_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final Instant DATETIME = Instant.parse("2026-08-10T19:30:00Z");
  private static final BigDecimal SEAT_PRICE = new BigDecimal("12.50");

  @Autowired private MockMvc mockMvc;
  @Autowired private JwtTokenProvider tokenProvider;
  @Autowired private ObjectMapper objectMapper;

  @org.springframework.boot.test.mock.mockito.MockBean private ProjectionService service;

  @Test
  void getAll_returnsProjections() throws Exception {
    when(service.getAll()).thenReturn(List.of(response()));

    mockMvc
        .perform(get("/projections").header("Authorization", bearer(ADMIN)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(ID.toString()))
        .andExpect(jsonPath("$[0].datetime").value("2026-08-10T19:30:00Z"))
        .andExpect(jsonPath("$[0].seatPrice").value(12.5))
        .andExpect(jsonPath("$[0].movie.id").value(MOVIE_ID.toString()))
        .andExpect(jsonPath("$[0].room.id").value(ROOM_ID.toString()));

    verify(service).getAll();
  }

  @Test
  void getById_returnsProjection() throws Exception {
    when(service.getById(ID)).thenReturn(model());

    mockMvc
        .perform(get("/projections/{id}", ID).header("Authorization", bearer(ADMIN)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(ID.toString()));

    verify(service).getById(ID);
  }

  @Test
  void create_persistsAndReturnsCreatedProjection() throws Exception {
    when(service.create(any(ProjectionRequest.class))).thenReturn(model());

    String body =
        objectMapper.writeValueAsString(
            Map.of(
                "datetime", DATETIME.toString(),
                "seatPrice", SEAT_PRICE,
                "movieId", MOVIE_ID.toString(),
                "roomId", ROOM_ID.toString()));

    mockMvc
        .perform(
            post("/projections")
                .header("Authorization", bearer(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(ID.toString()))
        .andExpect(jsonPath("$.movieId").value(MOVIE_ID.toString()))
        .andExpect(jsonPath("$.roomId").value(ROOM_ID.toString()));

    ArgumentCaptor<ProjectionRequest> captor = ArgumentCaptor.forClass(ProjectionRequest.class);
    verify(service).create(captor.capture());
    ProjectionRequest request = captor.getValue();
    org.assertj.core.api.Assertions.assertThat(request.datetime()).isEqualTo(DATETIME);
    org.assertj.core.api.Assertions.assertThat(request.seatPrice())
        .isEqualByComparingTo(SEAT_PRICE);
    org.assertj.core.api.Assertions.assertThat(request.movieId()).isEqualTo(MOVIE_ID);
    org.assertj.core.api.Assertions.assertThat(request.roomId()).isEqualTo(ROOM_ID);
  }

  @Test
  void update_savesAndReturnsProjection() throws Exception {
    when(service.update(any(ProjectionRequest.class), eq(ID))).thenReturn(model());

    String body =
        objectMapper.writeValueAsString(
            Map.of(
                "datetime", DATETIME.toString(),
                "seatPrice", SEAT_PRICE,
                "movieId", MOVIE_ID.toString(),
                "roomId", ROOM_ID.toString()));

    mockMvc
        .perform(
            put("/projections/{id}", ID)
                .header("Authorization", bearer(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(ID.toString()));

    verify(service).update(any(ProjectionRequest.class), eq(ID));
  }

  @Test
  void delete_removesProjection() throws Exception {
    mockMvc
        .perform(delete("/projections/{id}", ID).header("Authorization", bearer(ADMIN)))
        .andExpect(status().isNoContent());

    verify(service).delete(ID);
  }

  @Test
  void getById_returnsNotFound_whenMissing() throws Exception {
    when(service.getById(ID)).thenThrow(new EntityNotFoundException("Projection not found"));

    mockMvc
        .perform(get("/projections/{id}", ID).header("Authorization", bearer(ADMIN)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Projection not found"))
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void create_returnsBadRequest_whenBodyMalformed() throws Exception {
    mockMvc
        .perform(
            post("/projections")
                .header("Authorization", bearer(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{not json"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getById_returnsBadRequest_whenIdNotUuid() throws Exception {
    mockMvc
        .perform(get("/projections/{id}", "abc").header("Authorization", bearer(ADMIN)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getSeats_returnsAvailability() throws Exception {
    UUID seatId = UUID.fromString("44444444-4444-4444-4444-444444444444");
    when(service.getSeats(ID))
        .thenReturn(
            List.of(
                SeatAvailability.builder().seatId(seatId).number("A1").available(true).build()));

    mockMvc
        .perform(get("/projections/{id}/seats", ID).header("Authorization", bearer(CLIENT)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].seatId").value(seatId.toString()))
        .andExpect(jsonPath("$[0].number").value("A1"))
        .andExpect(jsonPath("$[0].available").value(true));

    verify(service).getSeats(ID);
  }

  @Test
  void getSeats_returnsUnauthorized_whenNoToken() throws Exception {
    mockMvc.perform(get("/projections/{id}/seats", ID)).andExpect(status().isUnauthorized());
    verify(service, never()).getSeats(any());
  }

  @Test
  void getAll_returnsUnauthorized_whenNoToken() throws Exception {
    mockMvc.perform(get("/projections")).andExpect(status().isUnauthorized());
    verify(service, never()).getAll();
  }

  @Test
  void getAll_returnsOk_forClientRole() throws Exception {
    when(service.getAll()).thenReturn(List.of());

    mockMvc
        .perform(get("/projections").header("Authorization", bearer(CLIENT)))
        .andExpect(status().isOk());
  }

  @Test
  void create_returnsForbidden_forClientRole() throws Exception {
    mockMvc
        .perform(
            post("/projections")
                .header("Authorization", bearer(CLIENT))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody()))
        .andExpect(status().isForbidden());
    verify(service, never()).create(any());
  }

  @Test
  void create_returnsForbidden_forEmployeeRole() throws Exception {
    mockMvc
        .perform(
            post("/projections")
                .header("Authorization", bearer(EMPLOYEE))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody()))
        .andExpect(status().isForbidden());
    verify(service, never()).create(any());
  }

  @Test
  void create_returnsCreated_forManagerRole() throws Exception {
    when(service.create(any(ProjectionRequest.class))).thenReturn(model());

    mockMvc
        .perform(
            post("/projections")
                .header("Authorization", bearer(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody()))
        .andExpect(status().isCreated());
  }

  private String requestBody() throws Exception {
    return objectMapper.writeValueAsString(
        Map.of(
            "datetime", DATETIME.toString(),
            "seatPrice", SEAT_PRICE,
            "movieId", MOVIE_ID.toString(),
            "roomId", ROOM_ID.toString()));
  }

  private String bearer(UserRole role) {
    return "Bearer " + token(role);
  }

  private String token(UserRole role) {
    User user = User.builder().id(UUID.randomUUID()).email("user@example.com").role(role).build();
    return tokenProvider.generateToken(user);
  }

  private Projection model() {
    return Projection.builder()
        .id(ID)
        .datetime(DATETIME)
        .seatPrice(SEAT_PRICE)
        .movieId(MOVIE_ID)
        .roomId(ROOM_ID)
        .build();
  }

  private ProjectionResponse response() {
    return ProjectionResponse.builder()
        .id(ID)
        .datetime(DATETIME)
        .seatPrice(SEAT_PRICE)
        .movie(MovieSummary.builder().id(MOVIE_ID).build())
        .room(RoomSummary.builder().id(ROOM_ID).build())
        .availableSeats(50)
        .build();
  }
}
