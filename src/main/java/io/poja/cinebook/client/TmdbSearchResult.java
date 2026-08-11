package io.poja.cinebook.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbSearchResult(
    int id, String title, String overview, String releaseDate, String posterPath) {}
