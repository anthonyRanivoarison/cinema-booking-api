package io.poja.cinebook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.poja.cinebook.conf.FacadeIT;
import io.poja.cinebook.dto.request.CreateReservationRequest;
import io.poja.cinebook.dto.request.ProjectionRequest;
import io.poja.cinebook.dto.response.SeatAvailability;
import io.poja.cinebook.entity.Projection;
import io.poja.cinebook.entity.enums.MovieGender;
import io.poja.cinebook.entity.enums.UserRole;
import io.poja.cinebook.repository.MovieRepository;
import io.poja.cinebook.repository.ProjectionRepository;
import io.poja.cinebook.repository.RoomRepository;
import io.poja.cinebook.repository.SeatRepository;
import io.poja.cinebook.repository.UserRepository;
import io.poja.cinebook.repository.model.JMovie;
import io.poja.cinebook.repository.model.JRoom;
import io.poja.cinebook.repository.model.JSeat;
import io.poja.cinebook.repository.model.JUser;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class ProjectionServiceIT extends FacadeIT {

  private static final Instant DATETIME = Instant.parse("2026-08-10T19:30:00Z");
  private static final BigDecimal SEAT_PRICE = new BigDecimal("12.50");

  @Autowired private ProjectionService service;
  @Autowired private ProjectionRepository projectionRepository;
  @Autowired private MovieRepository movieRepository;
  @Autowired private RoomRepository roomRepository;
  @Autowired private SeatRepository seatRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private ReservationService reservationService;

  @Test
  void getAll_returnsEmptyListOnEmptyDatabase() {
    assertThat(service.getAll()).isEmpty();
  }

  @Test
  void create_persistsProjectionWithResolvedMovieAndRoom() {
    JMovie movie = saveMovie("Dune");
    JRoom room = saveRoom("Salle 1");

    Projection created = service.create(request(movie.getId(), room.getId()));

    assertThat(created.id()).isNotNull();
    assertThat(created.datetime()).isEqualTo(DATETIME);
    assertThat(created.seatPrice()).isEqualByComparingTo(SEAT_PRICE);
    assertThat(created.movieId()).isEqualTo(movie.getId());
    assertThat(created.roomId()).isEqualTo(room.getId());

    assertThat(service.getAll()).containsExactly(created);
    assertThat(service.getById(created.id())).isEqualTo(created);
    assertThat(projectionRepository.count()).isEqualTo(1);
  }

  @Test
  void create_throwsNotFound_whenMovieMissing() {
    JRoom room = saveRoom("Salle 2");

    assertThatThrownBy(() -> service.create(request(UUID.randomUUID(), room.getId())))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Movie not found");
  }

  @Test
  void create_throwsNotFound_whenRoomMissing() {
    JMovie movie = saveMovie("Titanic");

    assertThatThrownBy(() -> service.create(request(movie.getId(), UUID.randomUUID())))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Room not found");
  }

  @Test
  void update_persistsChanges() {
    JMovie movie = saveMovie("Dune");
    JRoom room = saveRoom("Salle 3");
    Projection created = service.create(request(movie.getId(), room.getId()));

    Instant newDatetime = Instant.parse("2026-08-11T21:00:00Z");
    ProjectionRequest updateRequest =
        ProjectionRequest.builder()
            .datetime(newDatetime)
            .seatPrice(new BigDecimal("15.00"))
            .movieId(movie.getId())
            .roomId(room.getId())
            .build();

    Projection updated = service.update(updateRequest, created.id());

    assertThat(updated.id()).isEqualTo(created.id());
    assertThat(updated.datetime()).isEqualTo(newDatetime);
    assertThat(updated.seatPrice()).isEqualByComparingTo(new BigDecimal("15.00"));
    assertThat(service.getById(created.id())).isEqualTo(updated);
  }

  @Test
  void update_throwsNotFound_whenMissing() {
    assertThatThrownBy(
            () -> service.update(request(UUID.randomUUID(), UUID.randomUUID()), UUID.randomUUID()))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("not found");
  }

  @Test
  void delete_removesExistingProjection() {
    JMovie movie = saveMovie("Dune");
    JRoom room = saveRoom("Salle 4");
    Projection created = service.create(request(movie.getId(), room.getId()));

    service.delete(created.id());

    assertThat(projectionRepository.count()).isZero();
    assertThatThrownBy(() -> service.getById(created.id()))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Projection not found");
  }

  @Test
  void delete_throwsNotFound_whenMissing() {
    assertThatThrownBy(() -> service.delete(UUID.randomUUID()))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("not found");
  }

  @Test
  void getSeats_returnsAllSeatsAvailableOnFreshProjection() {
    JMovie movie = saveMovie("Dune");
    JRoom room = saveRoom("Salle 5");
    JSeat seatA = seatRepository.save(JSeat.builder().number("A1").room(room).build());
    JSeat seatB = seatRepository.save(JSeat.builder().number("A2").room(room).build());
    Projection projection = service.create(request(movie.getId(), room.getId()));

    List<SeatAvailability> result = service.getSeats(projection.id());

    assertThat(result).hasSize(2);
    assertThat(result).extracting(SeatAvailability::number).containsExactlyInAnyOrder("A1", "A2");
    assertThat(result).allMatch(SeatAvailability::available);
  }

  @Test
  void getSeats_marksSeatUnavailableAfterReservation() {
    JMovie movie = saveMovie("Dune");
    JRoom room = saveRoom("Salle 6");
    JSeat seatA = seatRepository.save(JSeat.builder().number("A1").room(room).build());
    JSeat seatB = seatRepository.save(JSeat.builder().number("A2").room(room).build());
    Projection projection = service.create(request(movie.getId(), room.getId()));

    JUser user =
        userRepository.save(
            JUser.builder()
                .firstName("John")
                .lastName("Doe")
                .email("seats@example.com")
                .password("hashed-password")
                .role(UserRole.CLIENT)
                .build());
    reservationService.create(
        CreateReservationRequest.builder()
            .userId(user.getId())
            .projectionId(projection.id())
            .seatIds(List.of(seatA.getId()))
            .build());

    List<SeatAvailability> result = service.getSeats(projection.id());

    assertThat(result)
        .filteredOn(seat -> seat.number().equals("A1"))
        .singleElement()
        .satisfies(seat -> assertThat(seat.available()).isFalse());
    assertThat(result)
        .filteredOn(seat -> seat.number().equals("A2"))
        .singleElement()
        .satisfies(seat -> assertThat(seat.available()).isTrue());
  }

  @Test
  void getSeats_throwsNotFound_whenProjectionMissing() {
    assertThatThrownBy(() -> service.getSeats(UUID.randomUUID()))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Projection not found");
  }

  private ProjectionRequest request(UUID movieId, UUID roomId) {
    return ProjectionRequest.builder()
        .datetime(DATETIME)
        .seatPrice(SEAT_PRICE)
        .movieId(movieId)
        .roomId(roomId)
        .build();
  }

  private JMovie saveMovie(String title) {
    JMovie movie =
        JMovie.builder()
            .title(title)
            .gender(MovieGender.ACTION)
            .description("test movie")
            .duration(Duration.ofMinutes(148))
            .build();
    return movieRepository.save(movie);
  }

  private JRoom saveRoom(String number) {
    return roomRepository.save(JRoom.builder().number(number).capacity(50).build());
  }
}
