package io.poja.cinebook.repository.model;

import io.poja.cinebook.entity.enums.MovieGender;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "movie")
public class JMovie {
  @Id @GeneratedValue private UUID id;

  @Column(nullable = false)
  private String title;

  @Enumerated(EnumType.STRING)
  private MovieGender gender;

  @Column(length = 1000)
  private String description;

  private Duration duration;

  @OneToMany(mappedBy = "movie")
  private List<JProjection> projections;
}
