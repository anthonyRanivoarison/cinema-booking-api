package io.poja.cinebook.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbMovie(
    int id,
    String title,
    String overview,
    Integer runtime,
    @JsonProperty("poster_path") String posterPath,
    List<TmdbGenre> genres) {}
