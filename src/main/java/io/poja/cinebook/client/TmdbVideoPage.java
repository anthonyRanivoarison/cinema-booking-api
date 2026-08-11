package io.poja.cinebook.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbVideoPage(List<TmdbVideo> results) {}
