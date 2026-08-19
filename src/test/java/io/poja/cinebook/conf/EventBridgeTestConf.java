package io.poja.cinebook.conf;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;

@TestConfiguration
public class EventBridgeTestConf {

  @Bean
  @Primary
  public EventBridgeClient eventBridgeClient() {
    EventBridgeClient client = mock(EventBridgeClient.class);
    when(client.putEvents(any(PutEventsRequest.class)))
        .thenReturn(PutEventsResponse.builder().entries(List.of()).build());
    return client;
  }
}
