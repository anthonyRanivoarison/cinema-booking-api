package io.poja.cinebook.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbMovie(
    int id,
    String title,
    String overview,
    int runtime,
    String posterPath,
    List<TmdbGenre> genres) {}
