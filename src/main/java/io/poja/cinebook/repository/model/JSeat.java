package io.poja.cinebook.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "seat")
public class JSeat {
  @Id @GeneratedValue private UUID id;

  @Column(nullable = false)
  private String number;

  @ManyToOne
  @JoinColumn(name = "room_id")
  private JRoom room;

  @ManyToMany(mappedBy = "seats")
  private List<JReservation> reservations;
}
