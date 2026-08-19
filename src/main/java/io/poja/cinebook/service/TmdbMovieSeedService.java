package io.poja.cinebook.service;

import io.poja.cinebook.client.TmdbClient;
import io.poja.cinebook.client.TmdbSearchResult;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class TmdbMovieSeedService {

  private static final int MOVIES_PER_PAGE = 20;
  private static final int PAGES_PER_RANGE = 2;

  private static final int[][] YEAR_RANGES = {
    {2024, 2026},
    {2020, 2023},
    {2015, 2019},
    {2010, 2014},
    {2005, 2009},
    {2000, 2004}
  };

  private final TmdbClient tmdbClient;
  private final MovieService movieService;

  @EventListener(ApplicationReadyEvent.class)
  public void seedPopularMovies() {
    log.info("Starting TMDB popular movies seed...");

    int imported = 0;
    int skipped = 0;
    int errors = 0;

    for (int[] range : YEAR_RANGES) {
      int yearFrom = range[0];
      int yearTo = range[1];

      for (int page = 1; page <= PAGES_PER_RANGE; page++) {
        List<TmdbSearchResult> movies;
        try {
          movies = tmdbClient.getPopularMovies(yearFrom, yearTo, page);
        } catch (Exception e) {
          log.warn(
              "Failed to fetch TMDB movies {}-{} page {}: {}",
              yearFrom,
              yearTo,
              page,
              e.getMessage());
          errors++;
          continue;
        }

        for (TmdbSearchResult result : movies) {
          if (result.id() <= 0) {
            continue;
          }
          try {
            movieService.importFromTmdb(result.id());
            imported++;
          } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("already imported")) {
              skipped++;
            } else {
              log.warn("Failed to import TMDB movie {}: {}", result.id(), e.getMessage());
              errors++;
            }
          }
        }
      }
    }

    log.info(
        "TMDB seed complete: {} imported, {} skipped (already existed), {} errors",
        imported,
        skipped,
        errors);
  }
}
