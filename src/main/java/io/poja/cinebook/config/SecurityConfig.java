package io.poja.cinebook.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final ObjectMapper objectMapper;

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, JwtAuthenticationConverter converter) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/error")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/auth/**")
                    .permitAll()
                    .requestMatchers("/ping")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/movies", "/movies/**")
                    .hasRole("MANAGER")
                    .requestMatchers(HttpMethod.POST, "/movies")
                    .hasRole("MANAGER")
                    .requestMatchers(HttpMethod.PUT, "/movies/*")
                    .hasRole("MANAGER")
                    .requestMatchers(HttpMethod.DELETE, "/movies/*")
                    .hasRole("MANAGER")
                    .requestMatchers(HttpMethod.GET, "/reservations")
                    .hasAnyRole("EMPLOYEE", "MANAGER")
                    .requestMatchers(HttpMethod.POST, "/reservations")
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, "/reservations/*")
                    .authenticated()
                    .requestMatchers(HttpMethod.PUT, "/reservations/*")
                    .hasAnyRole("MANAGER", "EMPLOYEE")
                    .requestMatchers(HttpMethod.GET, "/projections")
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, "/projections/*")
                    .authenticated()
                    .requestMatchers(HttpMethod.POST, "/projections")
                    .hasRole("MANAGER")
                    .requestMatchers(HttpMethod.PUT, "/projections/*")
                    .hasRole("MANAGER")
                    .requestMatchers(HttpMethod.DELETE, "/projections/*")
                    .hasRole("MANAGER")
                    .requestMatchers(HttpMethod.POST, "/users")
                    .hasRole("MANAGER")
                    .requestMatchers(HttpMethod.PATCH, "/users/{id}/role")
                    .hasRole("MANAGER")
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(
                        (req, res, e) ->
                            writeJsonError(res, 401, "Unauthorized: authentication required"))
                    .accessDeniedHandler(
                        (req, res, e) -> writeJsonError(res, 403, "Forbidden: insufficient role")));
    return http.build();
  }

  private void writeJsonError(HttpServletResponse res, int status, String message)
      throws IOException {
    res.setStatus(status);
    res.setContentType("application/json");
    res.getWriter()
        .write(
            objectMapper.writeValueAsString(
                Map.of("message", message, "status", status, "timestamp", Instant.now())));
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
