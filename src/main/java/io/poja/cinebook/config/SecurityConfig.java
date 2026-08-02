package io.poja.cinebook.config;

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
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, JwtAuthenticationConverter converter) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.POST, "/auth/**")
                    .permitAll()
                    .requestMatchers("/ping")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/movies", "/movies/**")
                    .authenticated()
                    .requestMatchers(HttpMethod.POST, "/movies")
                    .hasRole("MANAGER")
                    .requestMatchers(HttpMethod.PUT, "/movies**")
                    .hasRole("MANAGER")
                    .requestMatchers(HttpMethod.GET, "/reservations")
                    .hasAnyRole("EMPLOYEE", "MANAGER")
                    .requestMatchers(HttpMethod.POST, "/reservations")
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, "/reservations/")
                    .authenticated()
                    .requestMatchers(HttpMethod.PUT, "/reservations/")
                    .hasAnyRole("MANAGER", "EMPLOYEE")
                    .requestMatchers(HttpMethod.GET, "/projections")
                    .authenticated()
                    .requestMatchers(HttpMethod.PUT, "/projections/")
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
                ex.authenticationEntryPoint((req, res, e) -> res.sendError(401, "Unauthorized"))
                    .accessDeniedHandler((req, res, e) -> res.sendError(403, "Forbidden")));
    return http.build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
