package io.poja.cinebook.endpoint.rest.controller.movie;

import io.poja.cinebook.client.TmdbSearchResult;
import io.poja.cinebook.dto.request.MovieRequest;
import io.poja.cinebook.entity.Movie;
import io.poja.cinebook.service.MovieService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieController {
  private final MovieService service;

  @GetMapping
  public List<Movie> getAll() {
    return service.getAll();
  }

  @GetMapping("/{id}")
  public Movie getById(@PathVariable UUID id) {
    return service.getById(id);
  }

  @GetMapping("/search")
  public List<TmdbSearchResult> search(@RequestParam String query) {
    return service.searchTmdb(query);
  }

  @PostMapping("/import/{tmdbId}")
  @ResponseStatus(HttpStatus.CREATED)
  public Movie importFromTmdb(@PathVariable int tmdbId) {
    return service.importFromTmdb(tmdbId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Movie create(@RequestBody @Valid MovieRequest request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  public Movie update(@RequestBody @Valid MovieRequest request, @PathVariable UUID id) {
    return service.update(request, id);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    service.delete(id);
  }
}
