package io.poja.cinebook.endpoint.rest.controller.user;

import static io.poja.cinebook.entity.enums.UserRole.CLIENT;
import static io.poja.cinebook.entity.enums.UserRole.EMPLOYEE;
import static io.poja.cinebook.entity.enums.UserRole.MANAGER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.poja.cinebook.config.JwtConfig;
import io.poja.cinebook.config.JwtTokenProvider;
import io.poja.cinebook.config.SecurityConfig;
import io.poja.cinebook.dto.request.CreateUserRequest;
import io.poja.cinebook.dto.request.UpdateUserRoleRequest;
import io.poja.cinebook.dto.response.UserResponse;
import io.poja.cinebook.entity.User;
import io.poja.cinebook.entity.enums.UserRole;
import io.poja.cinebook.exception.GlobalExceptionHandler;
import io.poja.cinebook.service.UserService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
@Import({
  SecurityConfig.class,
  JwtConfig.class,
  JwtTokenProvider.class,
  GlobalExceptionHandler.class
})
class UserControllerTest {

  private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final String FIRST_NAME = "John";
  private static final String LAST_NAME = "Doe";
  private static final LocalDate BIRTH_DATE = LocalDate.of(1995, 5, 20);
  private static final String EMAIL = "john.doe@example.com";
  private static final String PHONE = "+261340000000";

  @Autowired private MockMvc mockMvc;
  @Autowired private JwtTokenProvider tokenProvider;
  @Autowired private ObjectMapper objectMapper;

  @org.springframework.boot.test.mock.mockito.MockBean private UserService service;

  @Test
  void getAll_returnsUsers() throws Exception {
    when(service.getAll()).thenReturn(List.of(userResponse(MANAGER)));

    mockMvc
        .perform(get("/users").header("Authorization", bearer(MANAGER)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(ID.toString()))
        .andExpect(jsonPath("$[0].firstName").value(FIRST_NAME))
        .andExpect(jsonPath("$[0].lastName").value(LAST_NAME))
        .andExpect(jsonPath("$[0].email").value(EMAIL))
        .andExpect(jsonPath("$[0].phone").value(PHONE))
        .andExpect(jsonPath("$[0].role").value(MANAGER.name()));

    verify(service).getAll();
  }

  @Test
  void create_returnsCreatedUser() throws Exception {
    when(service.createUser(any(CreateUserRequest.class))).thenReturn(userResponse(EMPLOYEE));

    mockMvc
        .perform(
            post("/users")
                .header("Authorization", bearer(MANAGER))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "firstName",
                            FIRST_NAME,
                            "lastName",
                            LAST_NAME,
                            "birthDate",
                            BIRTH_DATE.toString(),
                            "email",
                            EMAIL,
                            "password",
                            "password123",
                            "phone",
                            PHONE,
                            "role",
                            UserRole.EMPLOYEE.name()))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(ID.toString()))
        .andExpect(jsonPath("$.role").value(UserRole.EMPLOYEE.name()));

    ArgumentCaptor<CreateUserRequest> captor = ArgumentCaptor.forClass(CreateUserRequest.class);
    verify(service).createUser(captor.capture());
    CreateUserRequest request = captor.getValue();
    org.assertj.core.api.Assertions.assertThat(request.firstName()).isEqualTo(FIRST_NAME);
    org.assertj.core.api.Assertions.assertThat(request.email()).isEqualTo(EMAIL);
    org.assertj.core.api.Assertions.assertThat(request.role()).isEqualTo(UserRole.EMPLOYEE);
  }

  @Test
  void updateRole_updatesUserRole() throws Exception {
    when(service.updateRole(eq(ID), any(UpdateUserRoleRequest.class)))
        .thenReturn(userResponse(MANAGER));

    mockMvc
        .perform(
            patch("/users/{id}/role", ID)
                .header("Authorization", bearer(MANAGER))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"MANAGER\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(ID.toString()))
        .andExpect(jsonPath("$.role").value(MANAGER.name()));

    ArgumentCaptor<UpdateUserRoleRequest> captor =
        ArgumentCaptor.forClass(UpdateUserRoleRequest.class);
    verify(service).updateRole(eq(ID), captor.capture());
    org.assertj.core.api.Assertions.assertThat(captor.getValue().role()).isEqualTo(MANAGER);
  }

  @Test
  void getAll_returnsUnauthorized_whenNoToken() throws Exception {
    mockMvc.perform(get("/users")).andExpect(status().isUnauthorized());
    verify(service, never()).getAll();
  }

  @Test
  void getAll_returnsForbidden_forClientRole() throws Exception {
    mockMvc
        .perform(get("/users").header("Authorization", bearer(CLIENT)))
        .andExpect(status().isForbidden());
    verify(service, never()).getAll();
  }

  @Test
  void create_returnsForbidden_forClientRole() throws Exception {
    mockMvc
        .perform(
            post("/users")
                .header("Authorization", bearer(CLIENT))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isForbidden());
    verify(service, never()).createUser(any());
  }

  private UserResponse userResponse(UserRole role) {
    return UserResponse.builder()
        .id(ID)
        .firstName(FIRST_NAME)
        .lastName(LAST_NAME)
        .birthDate(BIRTH_DATE)
        .email(EMAIL)
        .phone(PHONE)
        .role(role)
        .build();
  }

  private String bearer(UserRole role) {
    return "Bearer " + token(role);
  }

  private String token(UserRole role) {
    User user = User.builder().id(UUID.randomUUID()).email("user@example.com").role(role).build();
    return tokenProvider.generateToken(user);
  }
}
