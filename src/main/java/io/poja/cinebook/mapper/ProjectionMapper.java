package io.poja.cinebook.mapper;

import io.poja.cinebook.dto.response.MovieSummary;
import io.poja.cinebook.dto.response.ProjectionResponse;
import io.poja.cinebook.dto.response.RoomSummary;
import io.poja.cinebook.entity.Projection;
import io.poja.cinebook.repository.model.JProjection;
import io.poja.cinebook.service.MovieService;
import io.poja.cinebook.service.RoomService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ProjectionMapper {
  private final MovieService movieService;
  private final RoomService roomService;
  private final MovieMapper movieMapper;
  private final RoomMapper roomMapper;

  public Projection toModel(JProjection entity) {
    return Projection.builder()
        .id(entity.getId())
        .datetime(entity.getDatetime())
        .seatPrice(entity.getSeatPrice())
        .movieId(entity.getMovie().getId())
        .roomId(entity.getRoom().getId())
        .build();
  }

  public List<Projection> toModel(List<JProjection> entities) {
    return entities.stream().map(this::toModel).toList();
  }

  public ProjectionResponse toResponse(JProjection entity, int availableSeats) {
    var movie =
        MovieSummary.builder()
            .id(entity.getMovie().getId())
            .title(entity.getMovie().getTitle())
            .posterUrl(entity.getMovie().getPosterUrl())
            .gender(entity.getMovie().getGender())
            .durationSeconds(
                entity.getMovie().getDuration() != null
                    ? entity.getMovie().getDuration().getSeconds()
                    : null)
            .build();

    var room =
        RoomSummary.builder()
            .id(entity.getRoom().getId())
            .number(entity.getRoom().getNumber())
            .capacity(entity.getRoom().getCapacity())
            .build();

    return ProjectionResponse.builder()
        .id(entity.getId())
        .datetime(entity.getDatetime())
        .seatPrice(entity.getSeatPrice())
        .movie(movie)
        .room(room)
        .availableSeats(availableSeats)
        .build();
  }

  public JProjection toEntity(Projection model) {
    var movie = movieService.getById(model.movieId());
    var room = roomService.getById(model.roomId());
    return JProjection.builder()
        .id(model.id())
        .datetime(model.datetime())
        .seatPrice(model.seatPrice())
        .movie(movieMapper.toEntity(movie))
        .room(roomMapper.toEntity(room))
        .build();
  }

  public List<JProjection> toEntity(List<Projection> models) {
    return models.stream().map(this::toEntity).toList();
  }
}
