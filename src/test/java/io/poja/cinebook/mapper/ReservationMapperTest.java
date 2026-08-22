package io.poja.cinebook.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.poja.cinebook.dto.response.ReservationResponse;
import io.poja.cinebook.entity.Projection;
import io.poja.cinebook.entity.Reservation;
import io.poja.cinebook.entity.Seat;
import io.poja.cinebook.entity.User;
import io.poja.cinebook.entity.enums.MovieGender;
import io.poja.cinebook.entity.enums.ReservationStatus;
import io.poja.cinebook.repository.model.JMovie;
import io.poja.cinebook.repository.model.JProjection;
import io.poja.cinebook.repository.model.JReservation;
import io.poja.cinebook.repository.model.JRoom;
import io.poja.cinebook.repository.model.JSeat;
import io.poja.cinebook.repository.model.JUser;
import io.poja.cinebook.service.ProjectionService;
import io.poja.cinebook.service.SeatService;
import io.poja.cinebook.service.UserService;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationMapperTest {

  private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID PROJECTION_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final UUID SEAT_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
  private static final UUID MOVIE_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
  private static final UUID ROOM_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
  private static final Instant CREATED_AT = Instant.parse("2026-08-05T10:00:00Z");
  private static final Instant DATETIME = Instant.parse("2026-08-10T19:30:00Z");

  @Mock private UserService userService;
  @Mock private ProjectionService projectionService;
  @Mock private SeatService seatService;
  @Mock private UserMapper userMapper;
  @Mock private ProjectionMapper projectionMapper;
  @Mock private SeatMapper seatMapper;

  private ReservationMapper mapper;

  @BeforeEach
  void setUp() {
    mapper =
        new ReservationMapper(
            userService, projectionService, seatService, userMapper, projectionMapper, seatMapper);
  }

  @Test
  void toModel_mapsAllFields() {
    Reservation result = mapper.toModel(entity());

    assertThat(result.id()).isEqualTo(ID);
    assertThat(result.createdAt()).isEqualTo(CREATED_AT);
    assertThat(result.userId()).isEqualTo(USER_ID);
    assertThat(result.projectionId()).isEqualTo(PROJECTION_ID);
    assertThat(result.seatIds()).containsExactly(SEAT_ID);
  }

  @Test
  void toModel_mapsList() {
    List<Reservation> result = mapper.toModel(List.of(entity(), entity()));

    assertThat(result).hasSize(2);
    assertThat(result.get(0).userId()).isEqualTo(USER_ID);
    assertThat(result.get(0).projectionId()).isEqualTo(PROJECTION_ID);
  }

  @Test
  void toModel_mapsEmptyList() {
    assertThat(mapper.toModel(List.of())).isEmpty();
  }

  @Test
  void toEntity_resolvesUserProjectionAndSeatsAndDelegatesMappers() {
    User user = user();
    Projection projection = projection();
    Seat seat = seat();
    when(userService.getById(USER_ID)).thenReturn(user);
    when(projectionService.getById(PROJECTION_ID)).thenReturn(projection);
    when(seatService.getById(SEAT_ID)).thenReturn(seat);
    when(userMapper.toEntity(user)).thenReturn(JUser.builder().id(USER_ID).build());
    when(projectionMapper.toEntity(projection))
        .thenReturn(JProjection.builder().id(PROJECTION_ID).build());
    when(seatMapper.toEntity(seat)).thenReturn(JSeat.builder().id(SEAT_ID).build());

    JReservation result = mapper.toEntity(model());

    verify(userService).getById(USER_ID);
    verify(projectionService).getById(PROJECTION_ID);
    verify(seatService).getById(SEAT_ID);
    verify(userMapper).toEntity(user);
    verify(projectionMapper).toEntity(projection);
    verify(seatMapper).toEntity(seat);
    assertThat(result.getId()).isEqualTo(ID);
    assertThat(result.getCreatedAt()).isEqualTo(CREATED_AT);
    assertThat(result.getUser().getId()).isEqualTo(USER_ID);
    assertThat(result.getProjection().getId()).isEqualTo(PROJECTION_ID);
    assertThat(result.getSeats()).hasSize(1);
    assertThat(result.getSeats().get(0).getId()).isEqualTo(SEAT_ID);
  }

  @Test
  void toEntity_mapsList() {
    when(userService.getById(any())).thenReturn(user());
    when(projectionService.getById(any())).thenReturn(projection());
    when(seatService.getById(any())).thenReturn(seat());
    when(userMapper.toEntity(any(User.class))).thenReturn(JUser.builder().id(USER_ID).build());
    when(projectionMapper.toEntity(any(Projection.class)))
        .thenReturn(JProjection.builder().id(PROJECTION_ID).build());
    when(seatMapper.toEntity(any(Seat.class))).thenReturn(JSeat.builder().id(SEAT_ID).build());

    List<JReservation> result = mapper.toEntity(List.of(model(), model()));

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getId()).isEqualTo(ID);
    assertThat(result.get(0).getUser().getId()).isEqualTo(USER_ID);
  }

  @Test
  void toEntity_mapsEmptyList() {
    assertThat(mapper.toEntity(List.of())).isEmpty();
  }

  @Test
  void toResponse_mapsAllFields() {
    JReservation entity = responseEntity();

    ReservationResponse result = mapper.toResponse(entity);

    assertThat(result.id()).isEqualTo(ID);
    assertThat(result.createdAt()).isEqualTo(CREATED_AT);
    assertThat(result.status()).isEqualTo(ReservationStatus.PENDING);
    assertThat(result.ticketUrl()).isEqualTo("https://ticket.example.com/123");
    assertThat(result.user().id()).isEqualTo(USER_ID);
    assertThat(result.user().firstName()).isEqualTo("Alice");
    assertThat(result.user().email()).isEqualTo("alice@example.com");
    assertThat(result.projection().id()).isEqualTo(PROJECTION_ID);
    assertThat(result.projection().datetime()).isEqualTo(DATETIME);
    assertThat(result.projection().seatPrice()).isEqualByComparingTo(new BigDecimal("15.00"));
    assertThat(result.projection().movie().id()).isEqualTo(MOVIE_ID);
    assertThat(result.projection().movie().title()).isEqualTo("Inception");
    assertThat(result.projection().movie().posterUrl())
        .isEqualTo("https://img.example.com/poster.jpg");
    assertThat(result.projection().movie().gender()).isEqualTo(MovieGender.ACTION);
    assertThat(result.projection().movie().durationSeconds())
        .isEqualTo(Duration.ofHours(2).getSeconds());
    assertThat(result.projection().room().id()).isEqualTo(ROOM_ID);
    assertThat(result.projection().room().number()).isEqualTo("A1");
    assertThat(result.projection().room().capacity()).isEqualTo(100);
    assertThat(result.seats()).hasSize(1);
    assertThat(result.seats().get(0).id()).isEqualTo(SEAT_ID);
    assertThat(result.seats().get(0).number()).isEqualTo("A1-S1");
  }

  @Test
  void toResponse_mapsList() {
    List<ReservationResponse> result =
        mapper.toResponse(List.of(responseEntity(), responseEntity()));

    assertThat(result).hasSize(2);
    assertThat(result.get(0).user().id()).isEqualTo(USER_ID);
    assertThat(result.get(0).projection().movie().id()).isEqualTo(MOVIE_ID);
  }

  @Test
  void toResponse_mapsEmptyList() {
    assertThat(mapper.toResponse(List.of())).isEmpty();
  }

  @Test
  void toResponse_handlesNullDuration() {
    JReservation entity = responseEntity();
    entity.getProjection().getMovie().setDuration(null);

    ReservationResponse result = mapper.toResponse(entity);

    assertThat(result.projection().movie().durationSeconds()).isNull();
  }

  private JReservation entity() {
    return JReservation.builder()
        .id(ID)
        .createdAt(CREATED_AT)
        .user(JUser.builder().id(USER_ID).build())
        .projection(JProjection.builder().id(PROJECTION_ID).build())
        .seats(List.of(JSeat.builder().id(SEAT_ID).build()))
        .build();
  }

  private Reservation model() {
    return Reservation.builder()
        .id(ID)
        .createdAt(CREATED_AT)
        .userId(USER_ID)
        .projectionId(PROJECTION_ID)
        .seatIds(List.of(SEAT_ID))
        .build();
  }

  private User user() {
    return User.builder().id(USER_ID).build();
  }

  private Projection projection() {
    return Projection.builder().id(PROJECTION_ID).build();
  }

  private Seat seat() {
    return Seat.builder().id(SEAT_ID).build();
  }

  private JReservation responseEntity() {
    var movie =
        JMovie.builder()
            .id(MOVIE_ID)
            .title("Inception")
            .posterUrl("https://img.example.com/poster.jpg")
            .gender(MovieGender.ACTION)
            .duration(Duration.ofHours(2))
            .build();
    var room = JRoom.builder().id(ROOM_ID).number("A1").capacity(100).build();
    var projection =
        JProjection.builder()
            .id(PROJECTION_ID)
            .datetime(DATETIME)
            .seatPrice(new BigDecimal("15.00"))
            .movie(movie)
            .room(room)
            .build();
    var user = JUser.builder().id(USER_ID).firstName("Alice").email("alice@example.com").build();
    var seat = JSeat.builder().id(SEAT_ID).number("A1-S1").build();
    return JReservation.builder()
        .id(ID)
        .createdAt(CREATED_AT)
        .status(ReservationStatus.PENDING)
        .ticketUrl("https://ticket.example.com/123")
        .user(user)
        .projection(projection)
        .seats(List.of(seat))
        .build();
  }
}
