package io.poja.cinebook.endpoint.rest.controller.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.poja.cinebook.config.JwtConfig;
import io.poja.cinebook.config.JwtTokenProvider;
import io.poja.cinebook.config.SecurityConfig;
import io.poja.cinebook.dto.request.LoginRequest;
import io.poja.cinebook.dto.request.SignUpRequest;
import io.poja.cinebook.dto.response.AuthResponse;
import io.poja.cinebook.entity.enums.UserRole;
import io.poja.cinebook.exception.GlobalExceptionHandler;
import io.poja.cinebook.service.AuthService;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AuthController.class)
@Import({
  SecurityConfig.class,
  JwtConfig.class,
  JwtTokenProvider.class,
  GlobalExceptionHandler.class
})
class AuthControllerTest {

  private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final String TOKEN = "jwt-token";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @org.springframework.boot.test.mock.mockito.MockBean private AuthService service;

  @Test
  void login_returnsAuthResponse() throws Exception {
    when(service.login(any(LoginRequest.class))).thenReturn(authResponse());

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of("email", "john.doe@example.com", "password", "password123"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value(TOKEN))
        .andExpect(jsonPath("$.userId").value(ID.toString()))
        .andExpect(jsonPath("$.role").value(UserRole.CLIENT.name()))
        .andExpect(jsonPath("$.email").value("john.doe@example.com"))
        .andExpect(jsonPath("$.firstName").value("John"));

    verify(service).login(any(LoginRequest.class));
  }

  @Test
  void signup_returnsCreatedAuthResponse() throws Exception {
    when(service.signup(any(SignUpRequest.class))).thenReturn(authResponse());

    mockMvc
        .perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "firstName",
                            "John",
                            "lastName",
                            "Doe",
                            "birthDate",
                            "1995-05-20",
                            "email",
                            "john.doe@example.com",
                            "password",
                            "password123",
                            "phone",
                            "+261340000000"))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.token").value(TOKEN))
        .andExpect(jsonPath("$.userId").value(ID.toString()))
        .andExpect(jsonPath("$.role").value(UserRole.CLIENT.name()))
        .andExpect(jsonPath("$.email").value("john.doe@example.com"))
        .andExpect(jsonPath("$.firstName").value("John"));

    verify(service).signup(any(SignUpRequest.class));
  }

  private AuthResponse authResponse() {
    return AuthResponse.builder()
        .token(TOKEN)
        .userId(ID)
        .role(UserRole.CLIENT)
        .email("john.doe@example.com")
        .firstName("John")
        .expiresIn(86400)
        .build();
  }
}
