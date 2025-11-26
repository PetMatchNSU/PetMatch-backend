package org.nsu.animal.repository;

import org.nsu.animal.entity.AnimalCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnimalCardRepository extends JpaRepository<AnimalCard, Long> {
	List<AnimalCard> findByCardAuthorIdOrderByCreatedDesc(Long userId);
}
