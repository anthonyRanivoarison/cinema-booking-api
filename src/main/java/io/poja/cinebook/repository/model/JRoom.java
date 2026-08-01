package io.poja.cinebook.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@Table(name = "room")
public class JRoom {
  @Id @GeneratedValue private UUID id;

  @Column(nullable = false)
  private String number;

  @Column(nullable = false)
  private int capacity;

  @OneToMany(mappedBy = "room")
  private List<JSeat> seats;

  @OneToMany(mappedBy = "room")
  private List<JProjection> projections;
}
