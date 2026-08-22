package io.poja.cinebook.endpoint.rest.controller.user;

import io.poja.cinebook.dto.request.CreateUserRequest;
import io.poja.cinebook.dto.request.UpdateUserRoleRequest;
import io.poja.cinebook.dto.response.UserResponse;
import io.poja.cinebook.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
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
@Tag(name = "Users", description = "User management and role assignment")
public class UserController {
  private final UserService service;

  @GetMapping("/me")
  @Operation(summary = "Current user", description = "Return the authenticated user's profile.")
  @ApiResponse(responseCode = "200", description = "User profile")
  public UserResponse getMe(@AuthenticationPrincipal Jwt jwt) {
    return service.getByEmail(jwt.getClaimAsString("email"));
  }

  @GetMapping
  @Operation(summary = "List users", description = "Return all users. ADMIN only.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "User list"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
  })
  public List<UserResponse> getAll() {
    return service.getAll();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = "Create user",
      description = "Create a new user with a specific role. ADMIN only.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "User created"),
    @ApiResponse(responseCode = "409", description = "Email already registered"),
  })
  public UserResponse create(@RequestBody CreateUserRequest request) {
    return service.createUser(request);
  }

  @PatchMapping("/{id}/role")
  @Operation(summary = "Update user role", description = "Change a user's role. ADMIN only.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Role updated"),
    @ApiResponse(responseCode = "404", description = "User not found"),
  })
  public UserResponse updateRole(
      @Parameter(description = "User UUID") @PathVariable UUID id,
      @RequestBody UpdateUserRoleRequest request) {
    return service.updateRole(id, request);
  }
}
