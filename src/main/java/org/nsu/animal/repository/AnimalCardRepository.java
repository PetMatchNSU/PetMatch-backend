package org.nsu.animal.repository;

import org.nsu.animal.entity.AnimalCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@Repository
public interface AnimalCardRepository extends JpaRepository<AnimalCard, Long>, JpaSpecificationExecutor<AnimalCard> {
List<AnimalCard> findByCardAuthorIdOrderByCreatedDesc(Long userId);
	Page<AnimalCard> findAllByStatus_NameIgnoreCase(String statusName, Pageable pageable);
}
