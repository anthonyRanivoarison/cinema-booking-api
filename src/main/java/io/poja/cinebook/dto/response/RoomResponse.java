package io.poja.cinebook.dto.response;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record RoomResponse(UUID id, String number, int capacity, List<SeatInfo> seats) {

  @Builder
  public record SeatInfo(UUID id, String number) {}
}
