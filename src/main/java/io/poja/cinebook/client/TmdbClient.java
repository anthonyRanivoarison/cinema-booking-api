package io.poja.cinebook.client;

import io.poja.cinebook.exception.ApiException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TmdbClient {

  private static final String POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500";

  private final String apiUrl;
  private final String apiKey;

  public TmdbClient(
      @Value("${tmdb.api.url:https://api.themoviedb.org/3}") String apiUrl,
      @Value("${tmdb.api.key:}") String apiKey) {
    this.apiUrl = apiUrl;
    this.apiKey = apiKey;
  }

  public TmdbMovie getMovie(int tmdbId) {
    return client().get().uri("/movie/{id}", tmdbId).retrieve().body(TmdbMovie.class);
  }

  public String getTrailerYoutubeKey(int tmdbId) {
    TmdbVideoPage page =
        client().get().uri("/movie/{id}/videos", tmdbId).retrieve().body(TmdbVideoPage.class);
    if (page == null || page.results() == null) {
      return null;
    }
    return page.results().stream()
        .filter(v -> "YouTube".equalsIgnoreCase(v.site()))
        .filter(v -> "Trailer".equalsIgnoreCase(v.type()) || "Teaser".equalsIgnoreCase(v.type()))
        .map(TmdbVideo::key)
        .findFirst()
        .orElse(null);
  }

  public List<TmdbSearchResult> search(String query) {
    TmdbSearchPage page =
        client()
            .get()
            .uri("/search/movie?query={query}", query)
            .retrieve()
            .body(TmdbSearchPage.class);
    return page == null || page.results() == null ? List.of() : page.results();
  }

  private RestClient client() {
    if (apiKey == null || apiKey.isBlank()) {
      throw new ApiException(
          "TMDB_API_KEY is not configured. Set it in your environment before importing movies.",
          HttpStatus.SERVICE_UNAVAILABLE);
    }
    return RestClient.builder()
        .baseUrl(apiUrl)
        .defaultHeader("Authorization", "Bearer " + apiKey)
        .defaultStatusHandler(
            HttpStatusCode::isError,
            (request, response) -> {
              HttpStatusCode code = response.getStatusCode();
              HttpStatus status =
                  code.value() == 404 ? HttpStatus.NOT_FOUND : HttpStatus.BAD_GATEWAY;
              throw new ApiException("TMDB request failed with status " + code.value(), status);
            })
        .build();
  }

  public static String composePosterUrl(String posterPath) {
    return posterPath == null || posterPath.isBlank() ? null : POSTER_BASE_URL + posterPath;
  }
}
