package io.poja.cinebook.dto.request;

import io.poja.cinebook.entity.enums.UserRole;
import lombok.Builder;

@Builder
public record UpdateUserRoleRequest(UserRole role) {}
