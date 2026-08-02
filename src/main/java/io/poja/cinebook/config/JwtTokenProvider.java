package io.poja.cinebook.config;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import io.poja.cinebook.entity.User;
import java.time.Instant;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

  @Value("${security.jwt.secret}")
  private String secret;

  @Value("${security.jwt.expiration-ms}")
  private long expirationMs;

  @Bean
  JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withSecretKey(secretKey()).macAlgorithm(MacAlgorithm.HS256).build();
  }

  public String generateToken(User user) {
    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .subject(user.id().toString())
            .claim("email", user.email())
            .claim("role", user.role().name())
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusMillis(expirationMs))
            .build();

    return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey()))
        .encode(JwtEncoderParameters.from(header, claims))
        .getTokenValue();
  }

  private SecretKey secretKey() {
    return new SecretKeySpec(secret.getBytes(UTF_8), "HmacSHA256");
  }
}
