package io.poja.cinebook.service;

import io.poja.cinebook.config.JwtTokenProvider;
import io.poja.cinebook.dto.request.LoginRequest;
import io.poja.cinebook.dto.request.SignUpRequest;
import io.poja.cinebook.dto.response.AuthResponse;
import io.poja.cinebook.endpoint.event.EventProducer;
import io.poja.cinebook.endpoint.event.model.SendEmailRequested;
import io.poja.cinebook.entity.User;
import io.poja.cinebook.entity.enums.UserRole;
import io.poja.cinebook.exception.ApiException;
import io.poja.cinebook.mapper.UserMapper;
import io.poja.cinebook.repository.UserRepository;
import io.poja.cinebook.repository.model.JUser;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final UserRepository repository;
  private final UserMapper mapper;
  private final JwtTokenProvider jwtProvider;
  private final PasswordEncoder passwordEncoder;
  private final EventProducer<SendEmailRequested> eventProducer;
  private final long expirationMs;

  public AuthService(
      UserRepository repository,
      UserMapper mapper,
      JwtTokenProvider jwtProvider,
      PasswordEncoder passwordEncoder,
      EventProducer<SendEmailRequested> eventProducer,
      @Value("${security.jwt.expiration-ms}") long expirationMs) {
    this.repository = repository;
    this.mapper = mapper;
    this.jwtProvider = jwtProvider;
    this.passwordEncoder = passwordEncoder;
    this.eventProducer = eventProducer;
    this.expirationMs = expirationMs;
  }

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
    eventProducer.accept(
        List.of(
            SendEmailRequested.builder()
                .to(user.email())
                .subject("Welcome to Cinema Booking!")
                .htmlBody(welcomeHtml(user.firstName()))
                .build()));
    return AuthResponse.builder()
        .token(jwtProvider.generateToken(user))
        .userId(user.id())
        .role(user.role())
        .email(user.email())
        .firstName(user.firstName())
        .expiresIn(expirationMs / 1000)
        .build();
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
    return AuthResponse.builder()
        .token(jwtProvider.generateToken(user))
        .userId(user.id())
        .role(user.role())
        .email(user.email())
        .firstName(user.firstName())
        .expiresIn(expirationMs / 1000)
        .build();
  }

  private String welcomeHtml(String firstName) {
    return """
           <html>
             <body>
               <p>Dear %s,</p>
               <p>Welcome to Cinema Booking! Your account has been created successfully.</p>
               <p>You can now book your seats for your favorite movies.</p>
               <p>Best regards,<br>The Cinema Booking Team</p>
             </body>
           </html>
           """
        .formatted(firstName);
  }
}
