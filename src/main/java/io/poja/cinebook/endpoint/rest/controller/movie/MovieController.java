package io.poja.cinebook.endpoint.rest.controller.movie;

import io.poja.cinebook.client.TmdbSearchResult;
import io.poja.cinebook.dto.request.MovieRequest;
import io.poja.cinebook.entity.Movie;
import io.poja.cinebook.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
@Tag(name = "Movies", description = "Movie catalog management")
public class MovieController {
  private final MovieService service;

  @GetMapping
  @Operation(
      summary = "List movies",
      description = "Return a paginated list of all movies in the catalog.")
  @ApiResponse(responseCode = "200", description = "Paginated movie list")
  public Page<Movie> getAll(
      @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
    return service.getAll(org.springframework.data.domain.PageRequest.of(page, size));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get movie by ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Movie found"),
    @ApiResponse(responseCode = "404", description = "Movie not found"),
  })
  public Movie getById(@PathVariable UUID id) {
    return service.getById(id);
  }

  @GetMapping("/search")
  @Operation(
      summary = "Search TMDB",
      description = "Search movies on TMDB by title. Does not import them.")
  @ApiResponse(responseCode = "200", description = "Search results from TMDB")
  public List<TmdbSearchResult> search(
      @Parameter(description = "Search query") @RequestParam String query) {
    return service.searchTmdb(query);
  }

  @PostMapping("/import/{tmdbId}")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = "Import from TMDB",
      description = "Import a movie from TMDB into the local catalog by its TMDB ID.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Movie imported"),
    @ApiResponse(responseCode = "404", description = "TMDB movie not found"),
  })
  public Movie importFromTmdb(@PathVariable int tmdbId) {
    return service.importFromTmdb(tmdbId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create movie", description = "Add a new movie to the catalog.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Movie created"),
    @ApiResponse(responseCode = "400", description = "Validation error"),
  })
  public Movie create(@RequestBody @Valid MovieRequest request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update movie")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Movie updated"),
    @ApiResponse(responseCode = "404", description = "Movie not found"),
    @ApiResponse(responseCode = "400", description = "Validation error"),
  })
  public Movie update(@RequestBody @Valid MovieRequest request, @PathVariable UUID id) {
    return service.update(request, id);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete movie")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Movie deleted"),
    @ApiResponse(responseCode = "404", description = "Movie not found"),
  })
  public void delete(@PathVariable UUID id) {
    service.delete(id);
  }
}
