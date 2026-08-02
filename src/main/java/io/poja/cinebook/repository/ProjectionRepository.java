package io.poja.cinebook.repository;

import io.poja.cinebook.repository.model.JProjection;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectionRepository extends JpaRepository<JProjection, UUID> {}
