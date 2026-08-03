package io.poja.cinebook.service;

import io.poja.cinebook.dto.request.CreateUserRequest;
import io.poja.cinebook.dto.request.UpdateUserRoleRequest;
import io.poja.cinebook.dto.response.UserResponse;
import io.poja.cinebook.entity.User;
import io.poja.cinebook.exception.ApiException;
import io.poja.cinebook.mapper.UserMapper;
import io.poja.cinebook.repository.UserRepository;
import io.poja.cinebook.repository.model.JUser;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
  private final UserMapper mapper;
  private final UserRepository repository;
  private final PasswordEncoder passwordEncoder;

  public User getById(UUID id) {
    return mapper.toModel(
        repository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found")));
  }

  public List<UserResponse> getAll() {
    return repository.findAll().stream().map(this::toResponse).toList();
  }

  public UserResponse createUser(CreateUserRequest request) {
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
            .role(request.role())
            .build();
    return toResponse(repository.save(mapper.toEntity(user)));
  }

  public UserResponse updateRole(UUID id, UpdateUserRoleRequest request) {
    JUser entity =
        repository
            .findById(id)
            .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
    entity.setRole(request.role());
    return toResponse(repository.save(entity));
  }

  private UserResponse toResponse(JUser entity) {
    return UserResponse.builder()
        .id(entity.getId())
        .firstName(entity.getFirstName())
        .lastName(entity.getLastName())
        .birthDate(entity.getBirthDate())
        .email(entity.getEmail())
        .phone(entity.getPhone())
        .role(entity.getRole())
        .build();
  }
}
