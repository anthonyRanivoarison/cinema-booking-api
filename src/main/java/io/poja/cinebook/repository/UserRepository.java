package io.poja.cinebook.repository;

import io.poja.cinebook.repository.model.JUser;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<JUser, UUID> {}
