package io.poja.cinebook.service.event;

import io.poja.cinebook.endpoint.event.model.SendEmailRequested;
import io.poja.cinebook.mail.Email;
import io.poja.cinebook.mail.Mailer;
import jakarta.mail.internet.InternetAddress;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SendEmailRequestedService implements Consumer<SendEmailRequested> {
  private final Mailer mailer;

  @Override
  @SneakyThrows
  public void accept(SendEmailRequested sendEmailRequested) {
    var recipientAddress = new InternetAddress(sendEmailRequested.getTo());
    mailer.accept(
        new Email(
            recipientAddress,
            List.of(),
            List.of(),
            sendEmailRequested.getSubject(),
            sendEmailRequested.getHtmlBody(),
            List.of()));
  }
}
