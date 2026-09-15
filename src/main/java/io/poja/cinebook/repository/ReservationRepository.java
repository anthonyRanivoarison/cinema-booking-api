package io.poja.cinebook.repository;

import io.poja.cinebook.entity.enums.ReservationStatus;
import io.poja.cinebook.repository.model.JReservation;
import io.poja.cinebook.repository.model.JSeat;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
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

  @Query(
      "SELECT r FROM JReservation r WHERE r.user.id = :userId AND r.idempotencyKey ="
          + " :idempotencyKey")
  Optional<JReservation> findByUserIdAndIdempotencyKey(
      @Param("userId") UUID userId, @Param("idempotencyKey") String idempotencyKey);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT s FROM JSeat s WHERE s.id IN :seatIds")
  List<JSeat> lockSeatsByIds(@Param("seatIds") List<UUID> seatIds);

  @Modifying(clearAutomatically = true)
  @Query("UPDATE JReservation r SET r.status = :status, r.ticketUrl = :ticketUrl WHERE r.id = :id")
  int updateStatusAndTicketUrl(
      @Param("id") UUID id,
      @Param("status") ReservationStatus status,
      @Param("ticketUrl") String ticketUrl);
}
