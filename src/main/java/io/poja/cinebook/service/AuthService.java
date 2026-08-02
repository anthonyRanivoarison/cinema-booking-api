package io.poja.cinebook.service;

import io.poja.cinebook.config.JwtTokenProvider;
import io.poja.cinebook.dto.request.LoginRequest;
import io.poja.cinebook.dto.request.SignUpRequest;
import io.poja.cinebook.dto.response.AuthResponse;
import io.poja.cinebook.entity.User;
import io.poja.cinebook.entity.enums.UserRole;
import io.poja.cinebook.exception.ApiException;
import io.poja.cinebook.mapper.UserMapper;
import io.poja.cinebook.repository.UserRepository;
import io.poja.cinebook.repository.model.JUser;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {
  private final UserRepository repository;
  private final UserMapper mapper;
  private final JwtTokenProvider jwtProvider;
  private final PasswordEncoder passwordEncoder;

  public AuthResponse signup(SignUpRequest request) {
    repository
        .findByEmail(request.email())
        .ifPresent(
            u -> {
              throw new ApiException("Email already registered", HttpStatus.CONFLICT);
            });
    User user =
        User.builder()
            .id(UUID.randomUUID())
            .firstName(request.firstName())
            .lastName(request.lastName())
            .birthDate(request.birthDate())
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .phone(request.phone())
            .role(UserRole.CLIENT)
            .build();
    repository.save(mapper.toEntity(user));
    return new AuthResponse(jwtProvider.generateToken(user), user.id(), user.role());
  }

  public AuthResponse login(LoginRequest request) {
    JUser entity =
        repository
            .findByEmail(request.email())
            .orElseThrow(() -> new ApiException("Invalid credentials", HttpStatus.UNAUTHORIZED));
    if (!passwordEncoder.matches(request.password(), entity.getPassword())) {
      throw new ApiException("Invalid password. Please try again", HttpStatus.UNAUTHORIZED);
    }
    User user = mapper.toModel(entity);
    return new AuthResponse(jwtProvider.generateToken(user), user.id(), user.role());
  }
}
