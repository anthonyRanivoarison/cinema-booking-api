package io.poja.cinebook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final String FIRST_NAME = "John";
  private static final String LAST_NAME = "Doe";
  private static final LocalDate BIRTH_DATE = LocalDate.of(1995, 5, 20);
  private static final String EMAIL = "john.doe@example.com";
  private static final String PASSWORD = "password123";
  private static final String ENCODED_PASSWORD = "encoded-password";
  private static final String PHONE = "+261340000000";
  private static final String TOKEN = "jwt-token";

  @Mock private UserRepository repository;
  @Mock private UserMapper mapper;
  @Mock private JwtTokenProvider jwtProvider;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private EventProducer<SendEmailRequested> eventProducer;
  @InjectMocks private AuthService service;

  @Test
  void signup_createsClientUserAndReturnsAuthResponse() {
    JUser saved = entity();
    when(repository.findByEmail(EMAIL)).thenReturn(Optional.empty());
    when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
    when(mapper.toEntity(any(User.class))).thenReturn(saved);
    when(repository.save(saved)).thenReturn(saved);
    when(jwtProvider.generateToken(any(User.class))).thenReturn(TOKEN);

    AuthResponse response = service.signup(signupRequest());

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(mapper).toEntity(captor.capture());
    User captured = captor.getValue();
    assertThat(captured.id()).isNotNull();
    assertThat(captured.firstName()).isEqualTo(FIRST_NAME);
    assertThat(captured.lastName()).isEqualTo(LAST_NAME);
    assertThat(captured.birthDate()).isEqualTo(BIRTH_DATE);
    assertThat(captured.email()).isEqualTo(EMAIL);
    assertThat(captured.password()).isEqualTo(ENCODED_PASSWORD);
    assertThat(captured.phone()).isEqualTo(PHONE);
    assertThat(captured.role()).isEqualTo(UserRole.CLIENT);
    verify(repository).save(saved);
    assertThat(response.token()).isEqualTo(TOKEN);
    assertThat(response.userId()).isEqualTo(captured.id());
    assertThat(response.role()).isEqualTo(UserRole.CLIENT);

    ArgumentCaptor<List<SendEmailRequested>> eventCaptor = ArgumentCaptor.forClass(List.class);
    verify(eventProducer).accept(eventCaptor.capture());
    SendEmailRequested event = eventCaptor.getValue().get(0);
    assertThat(event.getTo()).isEqualTo(EMAIL);
    assertThat(event.getSubject()).isEqualTo("Welcome to Cinema Booking!");
    assertThat(event.getHtmlBody()).contains("Welcome to Cinema Booking");
  }

  @Test
  void signup_throwsConflict_whenEmailAlreadyRegistered() {
    when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(entity()));

    assertThatThrownBy(() -> service.signup(signupRequest()))
        .isInstanceOf(ApiException.class)
        .hasMessage("Email already registered")
        .extracting(e -> ((ApiException) e).getStatus())
        .isEqualTo(HttpStatus.CONFLICT);
    verify(repository, never()).save(any());
    verify(passwordEncoder, never()).encode(any());
    verify(eventProducer, never()).accept(any());
  }

  @Test
  void login_returnsAuthResponse_whenValidCredentials() {
    JUser entity = entity();
    when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(entity));
    when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
    when(mapper.toModel(entity)).thenReturn(model());
    when(jwtProvider.generateToken(model())).thenReturn(TOKEN);

    AuthResponse response = service.login(loginRequest());

    assertThat(response.token()).isEqualTo(TOKEN);
    assertThat(response.userId()).isEqualTo(ID);
    assertThat(response.role()).isEqualTo(UserRole.CLIENT);
  }

  @Test
  void login_throwsUnauthorized_whenPasswordDoesNotMatch() {
    when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(entity()));
    when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

    assertThatThrownBy(() -> service.login(loginRequest()))
        .isInstanceOf(ApiException.class)
        .hasMessage("Invalid password. Please try again")
        .extracting(e -> ((ApiException) e).getStatus())
        .isEqualTo(HttpStatus.UNAUTHORIZED);
    verify(mapper, never()).toModel(any(JUser.class));
  }

  @Test
  void login_throwsUnauthorized_whenEmailNotFound() {
    when(repository.findByEmail(EMAIL)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.login(loginRequest()))
        .isInstanceOf(ApiException.class)
        .hasMessage("Invalid credentials")
        .extracting(e -> ((ApiException) e).getStatus())
        .isEqualTo(HttpStatus.UNAUTHORIZED);
    verify(mapper, never()).toModel(any(JUser.class));
  }

  private SignUpRequest signupRequest() {
    return SignUpRequest.builder()
        .firstName(FIRST_NAME)
        .lastName(LAST_NAME)
        .birthDate(BIRTH_DATE)
        .email(EMAIL)
        .password(PASSWORD)
        .phone(PHONE)
        .build();
  }

  private LoginRequest loginRequest() {
    return LoginRequest.builder().email(EMAIL).password(PASSWORD).build();
  }

  private JUser entity() {
    return JUser.builder()
        .id(ID)
        .firstName(FIRST_NAME)
        .lastName(LAST_NAME)
        .birthDate(BIRTH_DATE)
        .email(EMAIL)
        .password(ENCODED_PASSWORD)
        .phone(PHONE)
        .role(UserRole.CLIENT)
        .build();
  }

  private User model() {
    return User.builder()
        .id(ID)
        .firstName(FIRST_NAME)
        .lastName(LAST_NAME)
        .birthDate(BIRTH_DATE)
        .email(EMAIL)
        .password(ENCODED_PASSWORD)
        .phone(PHONE)
        .role(UserRole.CLIENT)
        .build();
  }
}
