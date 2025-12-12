package org.nsu.animal.repository;

import org.nsu.animal.entity.AnimalCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface AnimalCardRepository extends JpaRepository<AnimalCard, Long>, JpaSpecificationExecutor<AnimalCard> {
    List<AnimalCard> findByCardAuthorIdOrderByCreatedDesc(Long userId);

    Page<AnimalCard> findAllByStatus_NameIgnoreCase(String statusName, Pageable pageable);

    @Query("SELECT ac FROM AnimalCard ac WHERE " +
            "(:statuses IS NULL OR ac.status.name IN :statuses) AND " +
            "(:goals IS NULL OR ac.goal.goal IN :goals) AND " +
            "(:createdAt IS NULL OR ac.created >= :createdAt) AND " +
            "(:updatedAt IS NULL OR ac.updated >= :updatedAt) ")
    Page<AnimalCard> findByFilters(@Param("statuses") java.util.List<String> statuses,
            @Param("goals") java.util.List<String> goals,
            @Param("createdAt") LocalDateTime createdAt,
            @Param("updatedAt") LocalDateTime updatedAt,
            Pageable pageable);

    Page<AnimalCard> findAll(Pageable pageable);
}
