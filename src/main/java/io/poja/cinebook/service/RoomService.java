package io.poja.cinebook.service;

import io.poja.cinebook.dto.request.CreateRoomRequest;
import io.poja.cinebook.dto.response.RoomResponse;
import io.poja.cinebook.entity.Room;
import io.poja.cinebook.exception.ApiException;
import io.poja.cinebook.mapper.RoomMapper;
import io.poja.cinebook.repository.ProjectionRepository;
import io.poja.cinebook.repository.RoomRepository;
import io.poja.cinebook.repository.SeatRepository;
import io.poja.cinebook.repository.model.JRoom;
import io.poja.cinebook.repository.model.JSeat;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class RoomService {
  private static final String ROW_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  private final RoomMapper mapper;
  private final RoomRepository repository;
  private final SeatRepository seatRepository;
  private final ProjectionRepository projectionRepository;

  public Room getById(UUID id) {
    JRoom jRoom =
        repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Room not found"));
    List<JSeat> seats = seatRepository.findByRoom_Id(id);
    return mapper.toModel(jRoom, seats);
  }

  public RoomResponse getByIdWithDetails(UUID id) {
    JRoom jRoom =
        repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Room not found"));
    List<JSeat> seats = seatRepository.findByRoom_Id(id);
    return toRoomResponse(jRoom, seats);
  }

  public List<RoomResponse> getAll() {
    List<JRoom> jRooms = repository.findAll();
    return jRooms.stream()
        .map(
            jRoom -> {
              List<JSeat> seats = seatRepository.findByRoom_Id(jRoom.getId());
              return toRoomResponse(jRoom, seats);
            })
        .toList();
  }

  @Transactional
  public RoomResponse create(CreateRoomRequest request) {
    JRoom room = JRoom.builder().number(request.number()).capacity(request.capacity()).build();
    JRoom saved = repository.save(room);

    int rows = request.rows() != null ? request.rows() : calculateRows(request.capacity());
    int seatsPerRow =
        request.seatsPerRow() != null
            ? request.seatsPerRow()
            : (int) Math.ceil((double) request.capacity() / rows);

    List<JSeat> seats = generateSeats(saved, rows, seatsPerRow, request.capacity());
    seatRepository.saveAll(seats);

    return toRoomResponse(saved, seats);
  }

  @Transactional
  public RoomResponse update(UUID id, CreateRoomRequest request) {
    JRoom room =
        repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Room not found"));

    if (!room.getProjections().isEmpty()) {
      throw new ApiException("Cannot update room with existing projections", HttpStatus.CONFLICT);
    }

    room.setNumber(request.number());
    room.setCapacity(request.capacity());
    JRoom saved = repository.save(room);

    seatRepository.deleteAll(seatRepository.findByRoom_Id(id));

    int rows = request.rows() != null ? request.rows() : calculateRows(request.capacity());
    int seatsPerRow =
        request.seatsPerRow() != null
            ? request.seatsPerRow()
            : (int) Math.ceil((double) request.capacity() / rows);

    List<JSeat> seats = generateSeats(saved, rows, seatsPerRow, request.capacity());
    seatRepository.saveAll(seats);

    return toRoomResponse(saved, seats);
  }

  @Transactional
  public void delete(UUID id) {
    JRoom room =
        repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Room not found"));

    if (!room.getProjections().isEmpty()) {
      throw new ApiException("Cannot delete room with existing projections", HttpStatus.CONFLICT);
    }

    seatRepository.deleteAll(seatRepository.findByRoom_Id(id));
    repository.deleteById(id);
  }

  private List<JSeat> generateSeats(JRoom room, int rows, int seatsPerRow, int maxCapacity) {
    List<JSeat> seats = new ArrayList<>();
    int count = 0;

    for (int r = 0; r < rows && count < maxCapacity; r++) {
      char rowLetter = ROW_LETTERS.charAt(r % ROW_LETTERS.length());
      for (int s = 1; s <= seatsPerRow && count < maxCapacity; s++, count++) {
        String seatNumber = "" + rowLetter + s;
        seats.add(JSeat.builder().number(seatNumber).room(room).build());
      }
    }

    return seats;
  }

  private int calculateRows(int capacity) {
    int sqrt = (int) Math.sqrt(capacity);
    return Math.max(1, sqrt);
  }

  private RoomResponse toRoomResponse(JRoom jRoom, List<JSeat> seats) {
    List<RoomResponse.SeatInfo> seatInfos =
        seats.stream()
            .map(s -> RoomResponse.SeatInfo.builder().id(s.getId()).number(s.getNumber()).build())
            .toList();
    return RoomResponse.builder()
        .id(jRoom.getId())
        .number(jRoom.getNumber())
        .capacity(jRoom.getCapacity())
        .seats(seatInfos)
        .build();
  }
}
