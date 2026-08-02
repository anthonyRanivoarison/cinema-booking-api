package io.poja.cinebook.endpoint.rest.controller.user;

import io.poja.cinebook.dto.request.CreateUserRequest;
import io.poja.cinebook.dto.request.UpdateUserRoleRequest;
import io.poja.cinebook.dto.response.UserResponse;
import io.poja.cinebook.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
  private final UserService service;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse create(@RequestBody CreateUserRequest request) {
    return service.createUser(request);
  }

  @PatchMapping("/{id}/role")
  public UserResponse updateRole(
      @PathVariable UUID id, @RequestBody UpdateUserRoleRequest request) {
    return service.updateRole(id, request);
  }
}
