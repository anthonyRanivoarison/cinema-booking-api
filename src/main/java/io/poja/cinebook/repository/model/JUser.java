package io.poja.cinebook.repository.model;

import io.poja.cinebook.entity.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
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
@Table(name = "\"user\"")
public class JUser {
  @Id @GeneratedValue private UUID id;

  @Column(length = 200)
  private String firstName;

  @Column(length = 200)
  private String lastName;

  private LocalDate birthDate;

  @Column(unique = true)
  private String email;

  private String password;

  @Column(length = 50)
  private String phone;

  @Enumerated(EnumType.STRING)
  private UserRole role;

  @OneToMany(mappedBy = "user")
  private List<JReservation> reservations;
}
