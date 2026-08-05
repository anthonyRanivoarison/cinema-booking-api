package io.poja.cinebook.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.poja.cinebook.entity.Movie;
import io.poja.cinebook.entity.enums.MovieGender;
import io.poja.cinebook.repository.model.JMovie;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MovieMapperTest {

  private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final String TITLE = "Dune: Part Two";
  private static final MovieGender GENDER = MovieGender.ACTION;
  private static final String DESCRIPTION = "A young Paul Atreides continues his journey.";
  private static final Duration DURATION = Duration.ofMinutes(148);

  private final MovieMapper mapper = new MovieMapper();

  @Test
  void toModel_mapsAllFields() {
    Movie result = mapper.toModel(entity());

    assertThat(result.id()).isEqualTo(ID);
    assertThat(result.title()).isEqualTo(TITLE);
    assertThat(result.gender()).isEqualTo(GENDER);
    assertThat(result.description()).isEqualTo(DESCRIPTION);
    assertThat(result.duration()).isEqualTo(DURATION);
  }

  @Test
  void toModel_mapsList() {
    List<Movie> result = mapper.toModel(List.of(entity(), entity()));

    assertThat(result).hasSize(2);
    assertThat(result.get(0).title()).isEqualTo(TITLE);
    assertThat(result.get(1).duration()).isEqualTo(DURATION);
  }

  @Test
  void toModel_mapsEmptyList() {
    assertThat(mapper.toModel(List.of())).isEmpty();
  }

  @Test
  void toEntity_mapsAllFields() {
    JMovie result = mapper.toEntity(model());

    assertThat(result.getId()).isEqualTo(ID);
    assertThat(result.getTitle()).isEqualTo(TITLE);
    assertThat(result.getGender()).isEqualTo(GENDER);
    assertThat(result.getDescription()).isEqualTo(DESCRIPTION);
    assertThat(result.getDuration()).isEqualTo(DURATION);
  }

  @Test
  void toEntity_mapsList() {
    List<JMovie> result = mapper.toEntity(List.of(model(), model()));

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getId()).isEqualTo(ID);
    assertThat(result.get(1).getTitle()).isEqualTo(TITLE);
  }

  @Test
  void toEntity_mapsEmptyList() {
    assertThat(mapper.toEntity(List.of())).isEmpty();
  }

  private JMovie entity() {
    return JMovie.builder()
        .id(ID)
        .title(TITLE)
        .gender(GENDER)
        .description(DESCRIPTION)
        .duration(DURATION)
        .build();
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
