package io.poja.cinebook.endpoint.rest.controller.movie;

import static io.poja.cinebook.entity.enums.UserRole.CLIENT;
import static io.poja.cinebook.entity.enums.UserRole.EMPLOYEE;
import static io.poja.cinebook.entity.enums.UserRole.MANAGER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.poja.cinebook.client.TmdbSearchResult;
import io.poja.cinebook.config.JwtConfig;
import io.poja.cinebook.config.JwtTokenProvider;
import io.poja.cinebook.config.SecurityConfig;
import io.poja.cinebook.dto.request.MovieRequest;
import io.poja.cinebook.entity.Movie;
import io.poja.cinebook.entity.User;
import io.poja.cinebook.entity.enums.MovieGender;
import io.poja.cinebook.entity.enums.UserRole;
import io.poja.cinebook.exception.GlobalExceptionHandler;
import io.poja.cinebook.service.MovieService;
import jakarta.persistence.EntityNotFoundException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(MovieController.class)
@Import({
  SecurityConfig.class,
  JwtConfig.class,
  JwtTokenProvider.class,
  GlobalExceptionHandler.class
})
class MovieControllerTest {

  private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final String TITLE = "Dune: Part Two";
  private static final MovieGender GENDER = MovieGender.ACTION;
  private static final String DESCRIPTION = "A young Paul Atreides continues his journey.";
  private static final Duration DURATION = Duration.ofMinutes(148);

  @Autowired private MockMvc mockMvc;
  @Autowired private JwtTokenProvider tokenProvider;
  @Autowired private ObjectMapper objectMapper;

  @org.springframework.boot.test.mock.mockito.MockBean private MovieService service;

  @Test
  void getAll_returnsMovies() throws Exception {
    when(service.getAll()).thenReturn(List.of(model()));

    mockMvc
        .perform(get("/movies").header("Authorization", bearer(MANAGER)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(ID.toString()))
        .andExpect(jsonPath("$[0].title").value(TITLE))
        .andExpect(jsonPath("$[0].gender").value(GENDER.name()))
        .andExpect(jsonPath("$[0].description").value(DESCRIPTION))
        .andExpect(jsonPath("$[0].duration").value("PT2H28M"));

    verify(service).getAll();
  }

  @Test
  void getById_returnsMovie() throws Exception {
    when(service.getById(ID)).thenReturn(model());

    mockMvc
        .perform(get("/movies/{id}", ID).header("Authorization", bearer(MANAGER)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(ID.toString()))
        .andExpect(jsonPath("$.title").value(TITLE))
        .andExpect(jsonPath("$.duration").value("PT2H28M"));

    verify(service).getById(ID);
  }

  @Test
  void create_persistsAndReturnsCreatedMovie() throws Exception {
    when(service.create(any(MovieRequest.class))).thenReturn(model());

    mockMvc
        .perform(
            post("/movies")
                .header("Authorization", bearer(MANAGER))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(ID.toString()))
        .andExpect(jsonPath("$.title").value(TITLE))
        .andExpect(jsonPath("$.gender").value(GENDER.name()));

    ArgumentCaptor<MovieRequest> captor = ArgumentCaptor.forClass(MovieRequest.class);
    verify(service).create(captor.capture());
    MovieRequest request = captor.getValue();
    org.assertj.core.api.Assertions.assertThat(request.title()).isEqualTo(TITLE);
    org.assertj.core.api.Assertions.assertThat(request.gender()).isEqualTo(GENDER);
    org.assertj.core.api.Assertions.assertThat(request.description()).isEqualTo(DESCRIPTION);
    org.assertj.core.api.Assertions.assertThat(request.duration()).isEqualTo(DURATION);
  }

  @Test
  void update_savesAndReturnsMovie() throws Exception {
    when(service.update(any(MovieRequest.class), eq(ID))).thenReturn(model());

    mockMvc
        .perform(
            put("/movies/{id}", ID)
                .header("Authorization", bearer(MANAGER))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(ID.toString()));

    verify(service).update(any(MovieRequest.class), eq(ID));
  }

  @Test
  void delete_removesMovie() throws Exception {
    mockMvc
        .perform(delete("/movies/{id}", ID).header("Authorization", bearer(MANAGER)))
        .andExpect(status().isNoContent());

    verify(service).delete(ID);
  }

  @Test
  void getById_returnsNotFound_whenMissing() throws Exception {
    when(service.getById(ID)).thenThrow(new EntityNotFoundException("Movie not found"));

    mockMvc
        .perform(get("/movies/{id}", ID).header("Authorization", bearer(MANAGER)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Movie not found"))
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void create_returnsBadRequest_whenBodyMalformed() throws Exception {
    mockMvc
        .perform(
            post("/movies")
                .header("Authorization", bearer(MANAGER))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{not json"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void create_returnsBadRequest_whenValidationFails() throws Exception {
    mockMvc
        .perform(
            post("/movies")
                .header("Authorization", bearer(MANAGER))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"description\":\"missing title, gender and duration\"}"))
        .andExpect(status().isBadRequest());
    verify(service, never()).create(any());
  }

  @Test
  void getById_returnsBadRequest_whenIdNotUuid() throws Exception {
    mockMvc
        .perform(get("/movies/{id}", "abc").header("Authorization", bearer(MANAGER)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getAll_returnsUnauthorized_whenNoToken() throws Exception {
    mockMvc.perform(get("/movies")).andExpect(status().isUnauthorized());
    verify(service, never()).getAll();
  }

  @Test
  void getAll_returnsOk_forClientRole() throws Exception {
    when(service.getAll()).thenReturn(List.of(model()));

    mockMvc
        .perform(get("/movies").header("Authorization", bearer(CLIENT)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].title").value(TITLE));

    verify(service).getAll();
  }

  @Test
  void getAll_returnsOk_forEmployeeRole() throws Exception {
    when(service.getAll()).thenReturn(List.of(model()));

    mockMvc
        .perform(get("/movies").header("Authorization", bearer(EMPLOYEE)))
        .andExpect(status().isOk());

    verify(service).getAll();
  }

  @Test
  void create_returnsForbidden_forEmployeeRole() throws Exception {
    mockMvc
        .perform(
            post("/movies")
                .header("Authorization", bearer(EMPLOYEE))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody()))
        .andExpect(status().isForbidden());
    verify(service, never()).create(any());
  }

  @Test
  void create_returnsCreated_forManagerRole() throws Exception {
    when(service.create(any(MovieRequest.class))).thenReturn(model());

    mockMvc
        .perform(
            post("/movies")
                .header("Authorization", bearer(MANAGER))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody()))
        .andExpect(status().isCreated());
  }

  @Test
  void importFromTmdb_returnsCreated_forManagerRole() throws Exception {
    when(service.importFromTmdb(anyInt())).thenReturn(model());

    mockMvc
        .perform(post("/movies/import/{tmdbId}", 693134).header("Authorization", bearer(MANAGER)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value(TITLE));

    verify(service).importFromTmdb(693134);
  }

  @Test
  void importFromTmdb_returnsForbidden_forClientRole() throws Exception {
    mockMvc
        .perform(post("/movies/import/{tmdbId}", 693134).header("Authorization", bearer(CLIENT)))
        .andExpect(status().isForbidden());
    verify(service, never()).importFromTmdb(anyInt());
  }

  @Test
  void search_returnsResults_forManagerRole() throws Exception {
    when(service.searchTmdb("dune")).thenReturn(List.of(tmdbResult()));

    mockMvc
        .perform(
            get("/movies/search").param("query", "dune").header("Authorization", bearer(MANAGER)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].title").value("Dune"));

    verify(service).searchTmdb("dune");
  }

  @Test
  void search_returnsForbidden_forClientRole() throws Exception {
    mockMvc
        .perform(
            get("/movies/search").param("query", "dune").header("Authorization", bearer(CLIENT)))
        .andExpect(status().isForbidden());
    verify(service, never()).searchTmdb(any());
  }

  private String requestBody() throws Exception {
    return objectMapper.writeValueAsString(
        Map.of(
            "title",
            TITLE,
            "gender",
            GENDER.name(),
            "description",
            DESCRIPTION,
            "duration",
            DURATION.toString()));
  }

  private String bearer(UserRole role) {
    return "Bearer " + token(role);
  }

  private String token(UserRole role) {
    User user = User.builder().id(UUID.randomUUID()).email("user@example.com").role(role).build();
    return tokenProvider.generateToken(user);
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

  private TmdbSearchResult tmdbResult() {
    return new TmdbSearchResult(693134, "Dune", "A young Paul Atreides.", "2021-10-22", null);
  }
}
