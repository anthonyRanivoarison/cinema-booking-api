package io.poja.cinebook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.poja.cinebook.dto.request.MovieRequest;
import io.poja.cinebook.entity.Movie;
import io.poja.cinebook.entity.enums.MovieGender;
import io.poja.cinebook.exception.ApiException;
import io.poja.cinebook.mapper.MovieMapper;
import io.poja.cinebook.repository.MovieRepository;
import io.poja.cinebook.repository.model.JMovie;
import jakarta.persistence.EntityNotFoundException;
import java.time.Duration;
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
class MovieServiceTest {

  private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final String TITLE = "Dune: Part Two";
  private static final MovieGender GENDER = MovieGender.ACTION;
  private static final String DESCRIPTION = "A young Paul Atreides continues his journey.";
  private static final Duration DURATION = Duration.ofMinutes(148);

  @Mock private MovieRepository repository;
  @Mock private MovieMapper mapper;
  @InjectMocks private MovieService service;

  @Test
  void getAll_returnsMappedMovies() {
    var entity = entity();
    when(repository.findAll()).thenReturn(List.of(entity));
    when(mapper.toModel(List.of(entity))).thenReturn(List.of(model()));

    List<Movie> result = service.getAll();

    assertThat(result).containsExactly(model());
    verify(repository).findAll();
  }

  @Test
  void getById_returnsMovie_whenExists() {
    var entity = entity();
    when(repository.findById(ID)).thenReturn(Optional.of(entity));
    when(mapper.toModel(entity)).thenReturn(model());

    Movie result = service.getById(ID);

    assertThat(result).isEqualTo(model());
    verify(mapper).toModel(entity);
  }

  @Test
  void getById_throwsNotFound_whenMissing() {
    when(repository.findById(ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getById(ID))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Movie not found");
  }

  @Test
  void create_persistsNewMovieWithGeneratedId() {
    var request = request();
    var entity = entity();
    when(mapper.toEntity(any(Movie.class))).thenReturn(entity);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toModel(entity)).thenReturn(model());

    Movie result = service.create(request);

    ArgumentCaptor<Movie> captor = ArgumentCaptor.forClass(Movie.class);
    verify(mapper).toEntity(captor.capture());
    Movie captured = captor.getValue();
    assertThat(captured.id()).isNotNull();
    assertThat(captured.title()).isEqualTo(request.title());
    assertThat(captured.gender()).isEqualTo(request.gender());
    assertThat(captured.description()).isEqualTo(request.description());
    assertThat(captured.duration()).isEqualTo(request.duration());
    verify(repository).save(entity);
    verify(mapper).toModel(entity);
    assertThat(result).isEqualTo(model());
  }

  @Test
  void create_throwsBadRequest_whenDurationIsZero() {
    var request = request(Duration.ZERO);

    assertThatThrownBy(() -> service.create(request))
        .isInstanceOf(ApiException.class)
        .hasMessage("duration must be positive");
    verify(repository, never()).save(any());
  }

  @Test
  void create_throwsBadRequest_whenDurationIsNegative() {
    var request = request(Duration.ofMinutes(-10));

    assertThatThrownBy(() -> service.create(request))
        .isInstanceOf(ApiException.class)
        .hasMessage("duration must be positive");
    verify(repository, never()).save(any());
  }

  @Test
  void update_savesMovieWithRequestedId_whenExists() {
    var request = request();
    var entity = entity();
    when(repository.findById(ID)).thenReturn(Optional.of(entity));
    when(mapper.toEntity(any(Movie.class))).thenReturn(entity);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toModel(entity)).thenReturn(model());

    Movie result = service.update(request, ID);

    ArgumentCaptor<Movie> captor = ArgumentCaptor.forClass(Movie.class);
    verify(mapper).toEntity(captor.capture());
    Movie captured = captor.getValue();
    assertThat(captured.id()).isEqualTo(ID);
    assertThat(captured.title()).isEqualTo(request.title());
    assertThat(captured.duration()).isEqualTo(request.duration());
    verify(repository).save(entity);
    assertThat(result).isEqualTo(model());
  }

  @Test
  void update_throwsNotFound_whenMissing() {
    when(repository.findById(ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.update(request(), ID))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage(String.format("Movie with ID %s not found", ID));
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
        .hasMessage(String.format("Movie to delete with ID %s not found", ID));
    verify(repository, never()).deleteById(any(UUID.class));
  }

  private MovieRequest request() {
    return request(DURATION);
  }

  private MovieRequest request(Duration duration) {
    return MovieRequest.builder()
        .title(TITLE)
        .gender(GENDER)
        .description(DESCRIPTION)
        .duration(duration)
        .build();
  }

  private JMovie entity() {
    return JMovie.builder().id(ID).build();
  }

  private Movie model() {
    return Movie.builder()
        .id(ID)
        .title(TITLE)
        .gender(GENDER)
        .description(DESCRIPTION)
        .duration(DURATION)
        .build();
  }
}
