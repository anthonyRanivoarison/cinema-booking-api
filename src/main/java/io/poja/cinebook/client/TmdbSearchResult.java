package io.poja.cinebook.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbSearchResult(
    int id,
    String title,
    String overview,
    @JsonProperty("release_date") String releaseDate,
    @JsonProperty("poster_path") String posterPath) {}
