package io.poja.cinebook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.poja.cinebook.dto.request.ProjectionRequest;
import io.poja.cinebook.entity.Projection;
import io.poja.cinebook.mapper.ProjectionMapper;
import io.poja.cinebook.repository.ProjectionRepository;
import io.poja.cinebook.repository.model.JProjection;
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
  @Mock private ProjectionMapper mapper;
  @InjectMocks private ProjectionService service;

  @Test
  void getAll_returnsMappedProjections() {
    var entity = entity();
    when(repository.findAll()).thenReturn(List.of(entity));
    when(mapper.toModel(anyList())).thenReturn(List.of(model()));

    List<Projection> result = service.getAll();

    assertThat(result).containsExactly(model());
    verify(repository).findAll();
    verify(mapper).toModel(List.of(entity));
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
