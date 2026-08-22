package io.poja.cinebook.endpoint.rest.controller.auth;

import io.poja.cinebook.dto.request.LoginRequest;
import io.poja.cinebook.dto.request.SignUpRequest;
import io.poja.cinebook.dto.response.AuthResponse;
import io.poja.cinebook.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User registration and login")
public class AuthController {
  private final AuthService service;

  @PostMapping("/login")
  @Operation(
      summary = "Login",
      description = "Authenticate with email and password. Returns a JWT token.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Login successful"),
    @ApiResponse(responseCode = "401", description = "Invalid credentials"),
  })
  public AuthResponse login(@Validated @RequestBody LoginRequest request) {
    return service.login(request);
  }

  @PostMapping("/signup")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Sign up", description = "Register a new CLIENT account.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Account created"),
    @ApiResponse(responseCode = "409", description = "Email already registered"),
  })
  public AuthResponse signup(@Validated @RequestBody SignUpRequest request) {
    return service.signup(request);
  }
}
