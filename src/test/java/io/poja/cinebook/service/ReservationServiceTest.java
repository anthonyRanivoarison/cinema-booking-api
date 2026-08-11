package io.poja.cinebook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.poja.cinebook.dto.request.CreateReservationRequest;
import io.poja.cinebook.entity.Reservation;
import io.poja.cinebook.entity.enums.UserRole;
import io.poja.cinebook.exception.ApiException;
import io.poja.cinebook.exception.ForbiddenException;
import io.poja.cinebook.mapper.ReservationMapper;
import io.poja.cinebook.repository.ProjectionRepository;
import io.poja.cinebook.repository.ReservationRepository;
import io.poja.cinebook.repository.model.JReservation;
import jakarta.persistence.EntityNotFoundException;
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
class ReservationServiceTest {

  private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID OTHER_USER_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");
  private static final UUID PROJECTION_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final UUID SEAT_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
  private static final Instant CREATED_AT = Instant.parse("2026-08-05T10:00:00Z");

  @Mock private ReservationRepository repository;
  @Mock private ProjectionRepository projectionRepository;
  @Mock private ReservationMapper mapper;
  @InjectMocks private ReservationService service;

  @Test
  void getAll_returnsMappedReservations() {
    var entity = entity();
    when(repository.findAll()).thenReturn(List.of(entity));
    when(mapper.toModel(List.of(entity))).thenReturn(List.of(model()));

    List<Reservation> result = service.getAll();

    assertThat(result).containsExactly(model());
    verify(repository).findAll();
  }

  @Test
  void getById_returnsReservation_forManager() {
    var entity = entity();
    when(repository.findById(ID)).thenReturn(Optional.of(entity));
    when(mapper.toModel(entity)).thenReturn(model());

    Reservation result = service.getById(ID, OTHER_USER_ID.toString(), UserRole.MANAGER.name());

    assertThat(result).isEqualTo(model());
    verify(mapper).toModel(entity);
  }

  @Test
  void getById_returnsReservation_forOwnerClient() {
    var entity = entity();
    when(repository.findById(ID)).thenReturn(Optional.of(entity));
    when(mapper.toModel(entity)).thenReturn(model());

    Reservation result = service.getById(ID, USER_ID.toString(), UserRole.CLIENT.name());

    assertThat(result).isEqualTo(model());
  }

  @Test
  void getById_throwsForbidden_whenClientNotOwner() {
    var entity = entity();
    when(repository.findById(ID)).thenReturn(Optional.of(entity));
    when(mapper.toModel(entity)).thenReturn(model());

    assertThatThrownBy(() -> service.getById(ID, OTHER_USER_ID.toString(), UserRole.CLIENT.name()))
        .isInstanceOf(ForbiddenException.class)
        .hasMessage("Forbidden: a CLIENT can only access their own reservation");
  }

  @Test
  void getById_throwsNotFound_whenMissing() {
    when(repository.findById(ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getById(ID, USER_ID.toString(), UserRole.MANAGER.name()))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Reservation not found");
  }

  @Test
  void create_persistsNewReservationWithGeneratedId() {
    var request = request();
    var entity = entity();
    when(projectionRepository.existsById(PROJECTION_ID)).thenReturn(true);
    when(repository.findTakenSeatIdsByProjectionId(PROJECTION_ID)).thenReturn(List.of());
    when(mapper.toEntity(any(Reservation.class))).thenReturn(entity);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toModel(entity)).thenReturn(model());

    Reservation result = service.create(request);

    ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
    verify(mapper).toEntity(captor.capture());
    Reservation captured = captor.getValue();
    assertThat(captured.id()).isNotNull();
    assertThat(captured.createdAt()).isNotNull();
    assertThat(captured.userId()).isEqualTo(request.userId());
    assertThat(captured.projectionId()).isEqualTo(request.projectionId());
    assertThat(captured.seatIds()).isEqualTo(request.seatIds());
    verify(repository).save(entity);
    verify(mapper).toModel(entity);
    assertThat(result).isEqualTo(model());
  }

  @Test
  void create_throwsNotFound_whenProjectionMissing() {
    when(projectionRepository.existsById(PROJECTION_ID)).thenReturn(false);

    assertThatThrownBy(() -> service.create(request()))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Projection not found");
    verify(repository, never()).save(any());
  }

  @Test
  void create_throwsConflict_whenSeatAlreadyTaken() {
    when(projectionRepository.existsById(PROJECTION_ID)).thenReturn(true);
    when(repository.findTakenSeatIdsByProjectionId(PROJECTION_ID)).thenReturn(List.of(SEAT_ID));

    assertThatThrownBy(() -> service.create(request()))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("Seat(s) already reserved")
        .extracting(ex -> ((ApiException) ex).getStatus())
        .isEqualTo(org.springframework.http.HttpStatus.CONFLICT);
    verify(repository, never()).save(any());
  }

  @Test
  void update_savesReservation_whenExists() {
    var entity = entity();
    when(repository.findById(ID)).thenReturn(Optional.of(entity));
    when(mapper.toEntity(model())).thenReturn(entity);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toModel(entity)).thenReturn(model());

    Reservation result = service.update(model(), ID);

    verify(mapper).toEntity(model());
    verify(repository).save(entity);
    assertThat(result).isEqualTo(model());
  }

  @Test
  void update_throwsNotFound_whenMissing() {
    when(repository.findById(ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.update(model(), ID))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage(String.format("Reservation to update with ID %s not found", ID));
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
        .hasMessage(String.format("Reservation to delete with ID %s not found", ID));
    verify(repository, never()).deleteById(any(UUID.class));
  }

  private CreateReservationRequest request() {
    return CreateReservationRequest.builder()
        .userId(USER_ID)
        .projectionId(PROJECTION_ID)
        .seatIds(List.of(SEAT_ID))
        .build();
  }

  private JReservation entity() {
    return JReservation.builder().id(ID).build();
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
}
