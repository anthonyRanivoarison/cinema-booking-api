package io.poja.cinebook.dto.response;

import io.poja.cinebook.entity.enums.ReservationStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ReservationResponse(
    UUID id,
    Instant createdAt,
    ReservationStatus status,
    String ticketUrl,
    UserSummary user,
    ProjectionSummary projection,
    List<SeatInfo> seats) {}
