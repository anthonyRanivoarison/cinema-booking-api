package io.poja.cinebook.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.poja.cinebook.entity.Movie;
import io.poja.cinebook.entity.Projection;
import io.poja.cinebook.entity.Room;
import io.poja.cinebook.repository.model.JMovie;
import io.poja.cinebook.repository.model.JProjection;
import io.poja.cinebook.repository.model.JRoom;
import io.poja.cinebook.service.MovieService;
import io.poja.cinebook.service.RoomService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectionMapperTest {

  private static final UUID PROJECTION_ID = UUID.randomUUID();
  private static final UUID MOVIE_ID = UUID.randomUUID();
  private static final UUID ROOM_ID = UUID.randomUUID();
  private static final Instant DATETIME = Instant.parse("2026-08-10T19:30:00Z");
  private static final BigDecimal SEAT_PRICE = new BigDecimal("12.50");

  @Mock private MovieService movieService;
  @Mock private RoomService roomService;
  @Mock private MovieMapper movieMapper;
  @Mock private RoomMapper roomMapper;

  private ProjectionMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new ProjectionMapper(movieService, roomService, movieMapper, roomMapper);
  }

  @Test
  void toModel_mapsAllFields() {
    JProjection entity = entity();

    Projection result = mapper.toModel(entity);

    assertThat(result.id()).isEqualTo(PROJECTION_ID);
    assertThat(result.datetime()).isEqualTo(DATETIME);
    assertThat(result.seatPrice()).isEqualTo(SEAT_PRICE);
    assertThat(result.movieId()).isEqualTo(MOVIE_ID);
    assertThat(result.roomId()).isEqualTo(ROOM_ID);
  }

  @Test
  void toModel_mapsList() {
    List<Projection> result = mapper.toModel(List.of(entity(), entity()));

    assertThat(result).hasSize(2);
    assertThat(result.get(0).movieId()).isEqualTo(MOVIE_ID);
    assertThat(result.get(0).roomId()).isEqualTo(ROOM_ID);
  }

  @Test
  void toModel_mapsEmptyList() {
    assertThat(mapper.toModel(List.of())).isEmpty();
  }

  @Test
  void toEntity_resolvesMovieAndRoomAndDelegatesMappers() {
    Movie movie = movie();
    Room room = room();
    Projection model =
        Projection.builder()
            .id(PROJECTION_ID)
            .datetime(DATETIME)
            .seatPrice(SEAT_PRICE)
            .movieId(MOVIE_ID)
            .roomId(ROOM_ID)
            .build();
    when(movieService.getById(MOVIE_ID)).thenReturn(movie);
    when(roomService.getById(ROOM_ID)).thenReturn(room);
    when(movieMapper.toEntity(movie)).thenReturn(JMovie.builder().id(MOVIE_ID).build());
    when(roomMapper.toEntity(room)).thenReturn(JRoom.builder().id(ROOM_ID).build());

    JProjection result = mapper.toEntity(model);

    verify(movieService).getById(MOVIE_ID);
    verify(roomService).getById(ROOM_ID);
    verify(movieMapper).toEntity(movie);
    verify(roomMapper).toEntity(room);
    assertThat(result.getId()).isEqualTo(PROJECTION_ID);
    assertThat(result.getDatetime()).isEqualTo(DATETIME);
    assertThat(result.getSeatPrice()).isEqualTo(SEAT_PRICE);
    assertThat(result.getMovie().getId()).isEqualTo(MOVIE_ID);
    assertThat(result.getRoom().getId()).isEqualTo(ROOM_ID);
  }

  @Test
  void toEntity_mapsList() {
    when(movieService.getById(any())).thenReturn(movie());
    when(roomService.getById(any())).thenReturn(room());
    when(movieMapper.toEntity(any(Movie.class))).thenReturn(JMovie.builder().id(MOVIE_ID).build());
    when(roomMapper.toEntity(any(Room.class))).thenReturn(JRoom.builder().id(ROOM_ID).build());

    List<JProjection> result = mapper.toEntity(List.of(model(), model()));

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getId()).isEqualTo(PROJECTION_ID);
  }

  @Test
  void toEntity_mapsEmptyList() {
    assertThat(mapper.toEntity(List.of())).isEmpty();
  }

  private JProjection entity() {
    return JProjection.builder()
        .id(PROJECTION_ID)
        .datetime(DATETIME)
        .seatPrice(SEAT_PRICE)
        .movie(JMovie.builder().id(MOVIE_ID).build())
        .room(JRoom.builder().id(ROOM_ID).build())
        .build();
  }

  private Projection model() {
    return Projection.builder()
        .id(PROJECTION_ID)
        .datetime(DATETIME)
        .seatPrice(SEAT_PRICE)
        .movieId(MOVIE_ID)
        .roomId(ROOM_ID)
        .build();
  }

  private Movie movie() {
    return Movie.builder().id(MOVIE_ID).build();
  }

  private Room room() {
    return Room.builder().id(ROOM_ID).build();
  }
}
