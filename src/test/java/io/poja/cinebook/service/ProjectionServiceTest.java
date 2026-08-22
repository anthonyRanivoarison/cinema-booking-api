package io.poja.cinebook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.poja.cinebook.dto.request.ProjectionRequest;
import io.poja.cinebook.dto.response.ProjectionResponse;
import io.poja.cinebook.dto.response.SeatAvailability;
import io.poja.cinebook.entity.Projection;
import io.poja.cinebook.mapper.ProjectionMapper;
import io.poja.cinebook.repository.ProjectionRepository;
import io.poja.cinebook.repository.ReservationRepository;
import io.poja.cinebook.repository.SeatRepository;
import io.poja.cinebook.repository.model.JProjection;
import io.poja.cinebook.repository.model.JRoom;
import io.poja.cinebook.repository.model.JSeat;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectionServiceTest {

  private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID MOVIE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID ROOM_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final Instant DATETIME = Instant.parse("2026-08-10T19:30:00Z");
  private static final BigDecimal SEAT_PRICE = new BigDecimal("12.50");

  @Mock private ProjectionRepository repository;
  @Mock private SeatRepository seatRepository;
  @Mock private ReservationRepository reservationRepository;
  @Mock private ProjectionMapper mapper;
  @InjectMocks private ProjectionService service;

  @Test
  void getAll_returnsMappedProjections() {
    var entity = entity();
    JProjection fullEntity =
        JProjection.builder()
            .id(ID)
            .datetime(DATETIME)
            .seatPrice(SEAT_PRICE)
            .room(JRoom.builder().id(ROOM_ID).build())
            .build();
    ProjectionResponse projectionResponse =
        ProjectionResponse.builder()
            .id(ID)
            .datetime(DATETIME)
            .seatPrice(SEAT_PRICE)
            .availableSeats(5)
            .build();

    when(repository.findAll()).thenReturn(List.of(fullEntity));
    when(seatRepository.findByRoom_Id(ROOM_ID))
        .thenReturn(List.of(JSeat.builder().id(UUID.randomUUID()).build()));
    when(reservationRepository.findTakenOrPendingSeatIdsByProjectionId(ID)).thenReturn(List.of());
    when(mapper.toResponse(eq(fullEntity), eq(1))).thenReturn(projectionResponse);

    List<ProjectionResponse> result = service.getAll();

    assertThat(result).containsExactly(projectionResponse);
    verify(repository).findAll();
  }

  @Test
  void getById_returnsProjection_whenExists() {
    var entity = entity();
    when(repository.findById(ID)).thenReturn(Optional.of(entity));
    when(mapper.toModel(entity)).thenReturn(model());

    Projection result = service.getById(ID);

    assertThat(result).isEqualTo(model());
    verify(mapper).toModel(entity);
  }

  @Test
  void getById_throwsNotFound_whenMissing() {
    when(repository.findById(ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getById(ID))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Projection not found");
  }

  @Test
  void create_persistsNewProjectionWithGeneratedId() {
    var request = request();
    var entity = entity();
    when(mapper.toEntity(any(Projection.class))).thenReturn(entity);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toModel(entity)).thenReturn(model());

    Projection result = service.create(request);

    ArgumentCaptor<Projection> captor = ArgumentCaptor.forClass(Projection.class);
    verify(mapper).toEntity(captor.capture());
    Projection captured = captor.getValue();
    assertThat(captured.id()).isNotNull();
    assertThat(captured.datetime()).isEqualTo(request.datetime());
    assertThat(captured.seatPrice()).isEqualTo(request.seatPrice());
    assertThat(captured.movieId()).isEqualTo(request.movieId());
    assertThat(captured.roomId()).isEqualTo(request.roomId());
    verify(repository).save(entity);
    verify(mapper).toModel(entity);
    assertThat(result).isEqualTo(model());
  }

  @Test
  void create_propagatesNotFound_whenMovieOrRoomMissing() {
    when(mapper.toEntity(any(Projection.class)))
        .thenThrow(new EntityNotFoundException("Movie not found"));

    assertThatThrownBy(() -> service.create(request()))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Movie not found");
  }

  @Test
  void update_savesProjectionWithRequestedId_whenExists() {
    var request = request();
    var entity = entity();
    when(repository.findById(ID)).thenReturn(Optional.of(entity));
    when(mapper.toEntity(any(Projection.class))).thenReturn(entity);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toModel(entity)).thenReturn(model());

    Projection result = service.update(request, ID);

    ArgumentCaptor<Projection> captor = ArgumentCaptor.forClass(Projection.class);
    verify(mapper).toEntity(captor.capture());
    Projection captured = captor.getValue();
    assertThat(captured.id()).isEqualTo(ID);
    assertThat(captured.datetime()).isEqualTo(request.datetime());
    assertThat(captured.seatPrice()).isEqualTo(request.seatPrice());
    assertThat(captured.movieId()).isEqualTo(request.movieId());
    assertThat(captured.roomId()).isEqualTo(request.roomId());
    verify(repository).save(entity);
    assertThat(result).isEqualTo(model());
  }

  @Test
  void update_throwsNotFound_whenMissing() {
    when(repository.findById(ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.update(request(), ID))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage(String.format("Projection with ID %s not found", ID));
  }

  @Test
  void delete_deletes_whenExists() {
    when(repository.existsById(ID)).thenReturn(true);

    service.delete(ID);

    verify(repository).deleteById(ID);
  }

  @Test
  void delete_throwsNotFound_whenMissing() {
    when(repository.existsById(ID)).thenReturn(false);

    assertThatThrownBy(() -> service.delete(ID))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage(String.format("Projection to delete with ID %s not found", ID));
    verify(repository, never()).deleteById(any(UUID.class));
  }

  @Test
  void getSeats_returnsSeatsWithAvailability() {
    JProjection projection =
        JProjection.builder().id(ID).room(JRoom.builder().id(ROOM_ID).build()).build();
    UUID seatAId = UUID.fromString("44444444-4444-4444-4444-444444444444");
    UUID seatBId = UUID.fromString("55555555-5555-5555-5555-555555555555");
    JSeat seatA = JSeat.builder().id(seatAId).number("A1").build();
    JSeat seatB = JSeat.builder().id(seatBId).number("A2").build();
    when(repository.findById(ID)).thenReturn(Optional.of(projection));
    when(seatRepository.findByRoom_Id(ROOM_ID)).thenReturn(List.of(seatA, seatB));
    when(reservationRepository.findTakenOrPendingSeatIdsByProjectionId(ID))
        .thenReturn(List.of(seatBId));

    List<SeatAvailability> result = service.getSeats(ID);

    assertThat(result).hasSize(2);
    assertThat(result.get(0).seatId()).isEqualTo(seatAId);
    assertThat(result.get(0).number()).isEqualTo("A1");
    assertThat(result.get(0).available()).isTrue();
    assertThat(result.get(1).seatId()).isEqualTo(seatBId);
    assertThat(result.get(1).number()).isEqualTo("A2");
    assertThat(result.get(1).available()).isFalse();
  }

  @Test
  void getSeats_throwsNotFound_whenProjectionMissing() {
    when(repository.findById(ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getSeats(ID))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Projection not found");
  }

  private ProjectionRequest request() {
    return ProjectionRequest.builder()
        .datetime(DATETIME)
        .seatPrice(SEAT_PRICE)
        .movieId(MOVIE_ID)
        .roomId(ROOM_ID)
        .build();
  }

  private JProjection entity() {
    return JProjection.builder().id(ID).build();
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
}
