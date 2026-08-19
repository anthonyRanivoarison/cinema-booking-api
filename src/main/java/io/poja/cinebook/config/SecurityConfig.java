package io.poja.cinebook.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final ObjectMapper objectMapper;

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, JwtAuthenticationConverter converter) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/error")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/auth/**")
                    .permitAll()
                    .requestMatchers("/ping")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/movies/search")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/movies", "/movies/*")
                    .authenticated()
                    .requestMatchers(HttpMethod.POST, "/movies/import/*")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/movies")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/movies/*")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/movies/*")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/reservations")
                    .hasAnyRole("EMPLOYEE", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/reservations")
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, "/reservations/*")
                    .authenticated()
                    .requestMatchers(HttpMethod.PUT, "/reservations/*")
                    .hasAnyRole("ADMIN", "EMPLOYEE")
                    .requestMatchers(HttpMethod.DELETE, "/reservations/*")
                    .hasAnyRole("ADMIN", "EMPLOYEE")
                    .requestMatchers(HttpMethod.PATCH, "/reservations/*/approve")
                    .hasAnyRole("ADMIN", "EMPLOYEE")
                    .requestMatchers(HttpMethod.GET, "/projections")
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, "/projections/*")
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, "/projections/*/seats")
                    .authenticated()
                    .requestMatchers(HttpMethod.POST, "/projections")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/projections/*")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/projections/*")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/rooms")
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, "/rooms/*")
                    .authenticated()
                    .requestMatchers(HttpMethod.POST, "/rooms")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/rooms/*")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/rooms/*")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/users")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/users")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/users/{id}/role")
                    .hasRole("ADMIN")
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

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
