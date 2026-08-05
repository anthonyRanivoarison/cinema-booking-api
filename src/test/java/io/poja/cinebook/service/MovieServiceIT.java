package io.poja.cinebook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.poja.cinebook.conf.FacadeIT;
import io.poja.cinebook.dto.request.MovieRequest;
import io.poja.cinebook.entity.Movie;
import io.poja.cinebook.entity.enums.MovieGender;
import io.poja.cinebook.exception.ApiException;
import io.poja.cinebook.repository.MovieRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class MovieServiceIT extends FacadeIT {

  private static final String TITLE = "Dune: Part Two";
  private static final MovieGender GENDER = MovieGender.ACTION;
  private static final String DESCRIPTION = "A young Paul Atreides continues his journey.";
  private static final Duration DURATION = Duration.ofMinutes(148);

  @Autowired private MovieService service;
  @Autowired private MovieRepository movieRepository;

  @Test
  void getAll_returnsEmptyListOnEmptyDatabase() {
    assertThat(service.getAll()).isEmpty();
  }

  @Test
  void create_persistsMovie() {
    Movie created = service.create(request(DURATION));

    assertThat(created.id()).isNotNull();
    assertThat(created.title()).isEqualTo(TITLE);
    assertThat(created.gender()).isEqualTo(GENDER);
    assertThat(created.description()).isEqualTo(DESCRIPTION);
    assertThat(created.duration()).isEqualTo(DURATION);

    assertThat(service.getAll()).containsExactly(created);
    assertThat(service.getById(created.id())).isEqualTo(created);
    assertThat(movieRepository.count()).isEqualTo(1);
  }

  @Test
  void create_throwsBadRequest_whenDurationIsZero() {
    assertThatThrownBy(() -> service.create(request(Duration.ZERO)))
        .isInstanceOf(ApiException.class)
        .hasMessage("duration must be positive");
  }

  @Test
  void create_throwsBadRequest_whenDurationIsNegative() {
    assertThatThrownBy(() -> service.create(request(Duration.ofMinutes(-10))))
        .isInstanceOf(ApiException.class)
        .hasMessage("duration must be positive");
  }

  @Test
  void update_persistsChanges() {
    Movie created = service.create(request(DURATION));

    MovieRequest updateRequest =
        MovieRequest.builder()
            .title("Titanic")
            .gender(MovieGender.ROMANCE)
            .description("updated")
            .duration(Duration.ofMinutes(194))
            .build();

    Movie updated = service.update(updateRequest, created.id());

    assertThat(updated.id()).isEqualTo(created.id());
    assertThat(updated.title()).isEqualTo("Titanic");
    assertThat(updated.gender()).isEqualTo(MovieGender.ROMANCE);
    assertThat(updated.duration()).isEqualTo(Duration.ofMinutes(194));
    assertThat(service.getById(created.id())).isEqualTo(updated);
  }

  @Test
  void update_throwsNotFound_whenMissing() {
    assertThatThrownBy(() -> service.update(request(DURATION), UUID.randomUUID()))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("not found");
  }

  @Test
  void delete_removesExistingMovie() {
    Movie created = service.create(request(DURATION));

    service.delete(created.id());

    assertThat(movieRepository.count()).isZero();
    assertThatThrownBy(() -> service.getById(created.id()))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Movie not found");
  }

  @Test
  void delete_throwsNotFound_whenMissing() {
    assertThatThrownBy(() -> service.delete(UUID.randomUUID()))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("not found");
  }

  private MovieRequest request(Duration duration) {
    return MovieRequest.builder()
        .title(TITLE)
        .gender(GENDER)
        .description(DESCRIPTION)
        .duration(duration)
        .build();
  }
}
