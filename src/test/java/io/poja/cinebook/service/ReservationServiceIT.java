package io.poja.cinebook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.poja.cinebook.conf.FacadeIT;
import io.poja.cinebook.dto.request.CreateReservationRequest;
import io.poja.cinebook.dto.response.ReservationResponse;
import io.poja.cinebook.dto.response.SeatInfo;
import io.poja.cinebook.entity.Reservation;
import io.poja.cinebook.entity.enums.MovieGender;
import io.poja.cinebook.entity.enums.UserRole;
import io.poja.cinebook.exception.ApiException;
import io.poja.cinebook.exception.ForbiddenException;
import io.poja.cinebook.repository.MovieRepository;
import io.poja.cinebook.repository.ProjectionRepository;
import io.poja.cinebook.repository.ReservationRepository;
import io.poja.cinebook.repository.RoomRepository;
import io.poja.cinebook.repository.SeatRepository;
import io.poja.cinebook.repository.UserRepository;
import io.poja.cinebook.repository.model.JMovie;
import io.poja.cinebook.repository.model.JProjection;
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
class ReservationServiceIT extends FacadeIT {

  private static final Instant DATETIME = Instant.parse("2026-08-10T19:30:00Z");
  private static final BigDecimal SEAT_PRICE = new BigDecimal("12.50");
  private static final Duration MOVIE_DURATION = Duration.ofMinutes(148);

  @Autowired private ReservationService service;
  @Autowired private ReservationRepository reservationRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private MovieRepository movieRepository;
  @Autowired private RoomRepository roomRepository;
  @Autowired private SeatRepository seatRepository;
  @Autowired private ProjectionRepository projectionRepository;

  @Test
  void getAll_returnsEmptyListOnEmptyDatabase() {
    assertThat(service.getAll()).isEmpty();
  }

  @Test
  void create_persistsReservationWithResolvedEntities() {
    JUser user = saveUser("john@example.com");
    JProjection projection = saveProjection();
    JSeat seatA = saveSeat("A1");
    JSeat seatB = saveSeat("A2");

    ReservationResponse created =
        service.create(request(user.getId(), projection.getId(), seatA, seatB));

    assertThat(created.id()).isNotNull();
    assertThat(created.createdAt()).isNotNull();
    assertThat(created.user().id()).isEqualTo(user.getId());
    assertThat(created.projection().id()).isEqualTo(projection.getId());
    assertThat(created.seats().stream().map(SeatInfo::id).toList())
        .containsExactlyInAnyOrder(seatA.getId(), seatB.getId());

    assertThat(service.getAll())
        .hasSize(1)
        .first()
        .satisfies(r -> assertThat(r.id()).isEqualTo(created.id()));
    assertThat(
            service.getById(created.id(), UUID.randomUUID().toString(), UserRole.ADMIN.name()).id())
        .isEqualTo(created.id());
    assertThat(reservationRepository.count()).isEqualTo(1);
  }

  @Test
  void getById_returnsReservation_forOwnerClient() {
    JUser user = saveUser("jane@example.com");
    JProjection projection = saveProjection();
    JSeat seat = saveSeat("B1");
    ReservationResponse created = service.create(request(user.getId(), projection.getId(), seat));

    ReservationResponse result =
        service.getById(created.id(), user.getId().toString(), UserRole.CLIENT.name());

    assertThat(result.id()).isEqualTo(created.id());
  }

  @Test
  void getById_throwsForbidden_whenClientNotOwner() {
    JUser owner = saveUser("owner@example.com");
    JProjection projection = saveProjection();
    JSeat seat = saveSeat("C1");
    ReservationResponse created = service.create(request(owner.getId(), projection.getId(), seat));

    assertThatThrownBy(
            () ->
                service.getById(created.id(), UUID.randomUUID().toString(), UserRole.CLIENT.name()))
        .isInstanceOf(ForbiddenException.class)
        .hasMessage("Forbidden: a CLIENT can only access their own reservation");
  }

  @Test
  void getById_throwsNotFound_whenMissing() {
    assertThatThrownBy(
            () ->
                service.getById(
                    UUID.randomUUID(), UUID.randomUUID().toString(), UserRole.ADMIN.name()))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Reservation not found");
  }

  @Test
  void update_persistsChanges() {
    JUser user = saveUser("update@example.com");
    JProjection projection = saveProjection();
    JSeat seatA = saveSeat("D1");
    JSeat seatB = saveSeat("D2");
    ReservationResponse created =
        service.create(request(user.getId(), projection.getId(), seatA, seatB));

    Reservation updateRequest =
        Reservation.builder()
            .id(created.id())
            .createdAt(created.createdAt())
            .userId(user.getId())
            .projectionId(projection.getId())
            .seatIds(List.of(seatA.getId()))
            .build();

    ReservationResponse updated = service.update(updateRequest, created.id());

    assertThat(updated.id()).isEqualTo(created.id());
    assertThat(updated.seats().stream().map(SeatInfo::id).toList()).containsExactly(seatA.getId());
    assertThat(
            service
                .getById(created.id(), UUID.randomUUID().toString(), UserRole.ADMIN.name())
                .seats()
                .stream()
                .map(SeatInfo::id)
                .toList())
        .containsExactly(seatA.getId());
  }

  @Test
  void update_throwsNotFound_whenMissing() {
    Reservation dummy =
        Reservation.builder()
            .id(UUID.randomUUID())
            .createdAt(Instant.now())
            .userId(UUID.randomUUID())
            .projectionId(UUID.randomUUID())
            .seatIds(List.of(UUID.randomUUID()))
            .build();

    assertThatThrownBy(() -> service.update(dummy, UUID.randomUUID()))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("not found");
  }

  @Test
  void delete_removesExistingReservation() {
    JUser user = saveUser("delete@example.com");
    JProjection projection = saveProjection();
    JSeat seat = saveSeat("E1");
    ReservationResponse created = service.create(request(user.getId(), projection.getId(), seat));

    service.delete(created.id());

    assertThat(reservationRepository.count()).isZero();
    assertThatThrownBy(
            () ->
                service.getById(created.id(), UUID.randomUUID().toString(), UserRole.ADMIN.name()))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Reservation not found");
  }

  @Test
  void delete_throwsNotFound_whenMissing() {
    assertThatThrownBy(() -> service.delete(UUID.randomUUID()))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("not found");
  }

  @Test
  void create_throwsConflict_whenSeatAlreadyTaken() {
    JUser user = saveUser("conflict@example.com");
    JProjection projection = saveProjection();
    JSeat seat = saveSeat("F1");
    service.create(request(user.getId(), projection.getId(), seat));

    assertThatThrownBy(() -> service.create(request(user.getId(), projection.getId(), seat)))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("already reserved");
    assertThat(reservationRepository.count()).isEqualTo(1);
  }

  @Test
  void create_throwsNotFound_whenProjectionMissing() {
    JUser user = saveUser("missing@example.com");
    JSeat seat = saveSeat("F2");

    assertThatThrownBy(() -> service.create(request(user.getId(), UUID.randomUUID(), seat)))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Projection not found");
  }

  private CreateReservationRequest request(UUID userId, UUID projectionId, JSeat... seats) {
    return CreateReservationRequest.builder()
        .userId(userId)
        .projectionId(projectionId)
        .seatIds(java.util.Arrays.stream(seats).map(JSeat::getId).toList())
        .build();
  }

  private JUser saveUser(String email) {
    return userRepository.save(
        JUser.builder()
            .firstName("John")
            .lastName("Doe")
            .email(email)
            .password("hashed-password")
            .role(UserRole.CLIENT)
            .build());
  }

  private JProjection saveProjection() {
    JMovie movie =
        movieRepository.save(
            JMovie.builder()
                .title("Dune: Part Two")
                .gender(MovieGender.ACTION)
                .description("test movie")
                .duration(MOVIE_DURATION)
                .build());
    JRoom room = roomRepository.save(JRoom.builder().number("Salle 1").capacity(50).build());
    return projectionRepository.save(
        JProjection.builder()
            .datetime(DATETIME)
            .seatPrice(SEAT_PRICE)
            .movie(movie)
            .room(room)
            .build());
  }

  private JSeat saveSeat(String number) {
    JRoom room =
        roomRepository.save(JRoom.builder().number("Salle " + number).capacity(50).build());
    return seatRepository.save(JSeat.builder().number(number).room(room).build());
  }
}
