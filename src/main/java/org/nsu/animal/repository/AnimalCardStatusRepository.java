package org.nsu.animal.repository;

import org.nsu.animal.entity.AnimalCardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnimalCardStatusRepository extends JpaRepository<AnimalCardStatus, Long> {

    Optional<AnimalCardStatus> findByName(String name);
}
