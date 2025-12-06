package org.nsu.animal.repository;

import org.nsu.animal.entity.AnimalCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AnimalCardRepository extends JpaRepository<AnimalCard, Long>, JpaSpecificationExecutor<AnimalCard> {

	org.springframework.data.domain.Page<AnimalCard> findAllByStatus_NameIgnoreCase(String statusName, org.springframework.data.domain.Pageable pageable);
}
