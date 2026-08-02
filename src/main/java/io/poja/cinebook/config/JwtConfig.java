package io.poja.cinebook.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@Configuration
public class JwtConfig {

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    var converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(
        jwt -> {
          String role = jwt.getClaimAsString("role");
          if (role.isEmpty()) return List.of();
          return List.of(new SimpleGrantedAuthority("ROLE_" + role));
        });
    converter.setPrincipalClaimName("sub");
    return converter;
  }
}
