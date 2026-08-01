package io.poja.cinebook.endpoint.rest.controller.health;

import static io.poja.cinebook.endpoint.rest.controller.health.PingController.KO;
import static io.poja.cinebook.endpoint.rest.controller.health.PingController.OK;

import io.poja.cinebook.PojaGenerated;
import io.poja.cinebook.repository.DummyRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@PojaGenerated
@RestController
@AllArgsConstructor
public class HealthDbController {

  DummyRepository dummyRepository;

  @GetMapping("/health/db")
  public ResponseEntity<String> dummyTable_should_not_be_empty() {
    return dummyRepository.findAll().isEmpty() ? KO : OK;
  }
}
