package io.poja.cinebook.mapper;

import io.poja.cinebook.entity.User;
import io.poja.cinebook.repository.model.JUser;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
  public User toModel(JUser entity) {
    return User.builder()
        .id(entity.getId())
        .firstName(entity.getFirstName())
        .lastName(entity.getLastName())
        .birthDate(entity.getBirthDate())
        .email(entity.getEmail())
        .password(entity.getPassword())
        .phone(entity.getPhone())
        .role(entity.getRole())
        .build();
  }

  public List<User> toModel(List<JUser> entities) {
    return entities.stream().map(this::toModel).toList();
  }

  public JUser toEntity(User model) {
    return JUser.builder()
        .id(model.id())
        .firstName(model.firstName())
        .lastName(model.lastName())
        .birthDate(model.birthDate())
        .email(model.email())
        .password(model.password())
        .phone(model.phone())
        .role(model.role())
        .build();
  }

  public List<JUser> toEntity(List<User> models) {
    return models.stream().map(this::toEntity).toList();
  }
}
