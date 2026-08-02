package io.poja.cinebook.service;

import io.poja.cinebook.entity.User;
import io.poja.cinebook.mapper.UserMapper;
import io.poja.cinebook.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
  private final UserMapper mapper;
  private final UserRepository repository;

  public User getById(UUID id) {
    return mapper.toModel(
        repository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found")));
  }
}
