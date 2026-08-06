package io.poja.cinebook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.poja.cinebook.dto.request.CreateUserRequest;
import io.poja.cinebook.dto.request.UpdateUserRoleRequest;
import io.poja.cinebook.dto.response.UserResponse;
import io.poja.cinebook.entity.User;
import io.poja.cinebook.entity.enums.UserRole;
import io.poja.cinebook.exception.ApiException;
import io.poja.cinebook.mapper.UserMapper;
import io.poja.cinebook.repository.UserRepository;
import io.poja.cinebook.repository.model.JUser;
import jakarta.persistence.EntityNotFoundException;
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
class UserServiceTest {

  private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final String FIRST_NAME = "John";
  private static final String LAST_NAME = "Doe";
  private static final LocalDate BIRTH_DATE = LocalDate.of(1995, 5, 20);
  private static final String EMAIL = "john.doe@example.com";
  private static final String PASSWORD = "password123";
  private static final String ENCODED_PASSWORD = "encoded-password";
  private static final String PHONE = "+261340000000";

  @Mock private UserMapper mapper;
  @Mock private UserRepository repository;
  @Mock private PasswordEncoder passwordEncoder;
  @InjectMocks private UserService service;

  @Test
  void getById_returnsUser_whenExists() {
    JUser entity = entity(UserRole.CLIENT);
    when(repository.findById(ID)).thenReturn(Optional.of(entity));
    when(mapper.toModel(entity)).thenReturn(model());

    User result = service.getById(ID);

    assertThat(result).isEqualTo(model());
    verify(mapper).toModel(entity);
  }

  @Test
  void getById_throwsNotFound_whenMissing() {
    when(repository.findById(ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getById(ID))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("User not found");
  }

  @Test
  void getAll_returnsMappedUserResponses() {
    JUser entity = entity(UserRole.MANAGER);
    when(repository.findAll()).thenReturn(List.of(entity));

    List<UserResponse> result = service.getAll();

    assertThat(result).hasSize(1);
    UserResponse response = result.get(0);
    assertThat(response.id()).isEqualTo(ID);
    assertThat(response.firstName()).isEqualTo(FIRST_NAME);
    assertThat(response.lastName()).isEqualTo(LAST_NAME);
    assertThat(response.birthDate()).isEqualTo(BIRTH_DATE);
    assertThat(response.email()).isEqualTo(EMAIL);
    assertThat(response.phone()).isEqualTo(PHONE);
    assertThat(response.role()).isEqualTo(UserRole.MANAGER);
  }

  @Test
  void createUser_persistsAndReturnsUserResponse() {
    JUser saved = entity(UserRole.EMPLOYEE);
    when(repository.findByEmail(EMAIL)).thenReturn(Optional.empty());
    when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
    when(mapper.toEntity(any(User.class))).thenReturn(saved);
    when(repository.save(saved)).thenReturn(saved);

    UserResponse response = service.createUser(createUserRequest(UserRole.EMPLOYEE));

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
    assertThat(captured.role()).isEqualTo(UserRole.EMPLOYEE);
    assertThat(response.id()).isEqualTo(ID);
    assertThat(response.role()).isEqualTo(UserRole.EMPLOYEE);
    assertThat(response.email()).isEqualTo(EMAIL);
  }

  @Test
  void createUser_throwsConflict_whenEmailAlreadyRegistered() {
    when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(entity(UserRole.CLIENT)));

    assertThatThrownBy(() -> service.createUser(createUserRequest(UserRole.EMPLOYEE)))
        .isInstanceOf(ApiException.class)
        .hasMessage("Email already registered")
        .extracting(e -> ((ApiException) e).getStatus())
        .isEqualTo(HttpStatus.CONFLICT);
    verify(repository, never()).save(any());
    verify(passwordEncoder, never()).encode(any());
  }

  @Test
  void updateRole_updatesRoleAndReturnsUserResponse() {
    JUser entity = entity(UserRole.CLIENT);
    when(repository.findById(ID)).thenReturn(Optional.of(entity));
    when(repository.save(entity)).thenReturn(entity);

    UserResponse response = service.updateRole(ID, new UpdateUserRoleRequest(UserRole.MANAGER));

    verify(repository).save(entity);
    assertThat(entity.getRole()).isEqualTo(UserRole.MANAGER);
    assertThat(response.role()).isEqualTo(UserRole.MANAGER);
  }

  @Test
  void updateRole_throwsNotFound_whenMissing() {
    when(repository.findById(ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.updateRole(ID, new UpdateUserRoleRequest(UserRole.MANAGER)))
        .isInstanceOf(ApiException.class)
        .hasMessage("User not found")
        .extracting(e -> ((ApiException) e).getStatus())
        .isEqualTo(HttpStatus.NOT_FOUND);
    verify(repository, never()).save(any());
  }

  private CreateUserRequest createUserRequest(UserRole role) {
    return CreateUserRequest.builder()
        .firstName(FIRST_NAME)
        .lastName(LAST_NAME)
        .birthDate(BIRTH_DATE)
        .email(EMAIL)
        .password(PASSWORD)
        .phone(PHONE)
        .role(role)
        .build();
  }

  private JUser entity(UserRole role) {
    return JUser.builder()
        .id(ID)
        .firstName(FIRST_NAME)
        .lastName(LAST_NAME)
        .birthDate(BIRTH_DATE)
        .email(EMAIL)
        .password(ENCODED_PASSWORD)
        .phone(PHONE)
        .role(role)
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
