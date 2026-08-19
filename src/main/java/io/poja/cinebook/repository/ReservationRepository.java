package io.poja.cinebook.repository;

import io.poja.cinebook.repository.model.JReservation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<JReservation, UUID> {

  @Query(
      "SELECT s.id FROM JReservation r JOIN r.seats s WHERE r.projection.id = :projectionId AND"
          + " r.status = 'APPROVED'")
  List<UUID> findTakenSeatIdsByProjectionId(@Param("projectionId") UUID projectionId);

  @Query(
      "SELECT s.id FROM JReservation r JOIN r.seats s WHERE r.projection.id = :projectionId AND"
          + " r.status IN ('APPROVED', 'PENDING')")
  List<UUID> findTakenOrPendingSeatIdsByProjectionId(@Param("projectionId") UUID projectionId);

  List<JReservation> findByUserId(UUID userId);
}
