package io.poja.cinebook.repository;

import io.poja.cinebook.repository.model.JSeat;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<JSeat, UUID> {
  List<JSeat> findByRoom_Id(UUID roomId);
}
