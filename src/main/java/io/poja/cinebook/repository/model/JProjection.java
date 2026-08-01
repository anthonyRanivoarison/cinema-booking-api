package io.poja.cinebook.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
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
@Table(name = "projection")
public class JProjection {
  @Id @GeneratedValue private UUID id;

  @Column(nullable = false)
  private Instant datetime;

  private BigDecimal seatPrice;

  @ManyToOne
  @JoinColumn(name = "movie_id")
  private JMovie movie;

  @ManyToOne
  @JoinColumn(name = "room_id")
  private JRoom room;

  @OneToMany(mappedBy = "projection")
  private List<JReservation> reservations;
}
