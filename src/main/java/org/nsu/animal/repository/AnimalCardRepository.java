package org.nsu.animal.repository;

import org.nsu.animal.entity.AnimalCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
=======
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
>>>>>>> 3574cce ([PetMatch-15976] Модерация карточек животных для публикации в админке)
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

@Repository
public interface AnimalCardRepository extends JpaRepository<AnimalCard, Long>, JpaSpecificationExecutor<AnimalCard> {

    Page<AnimalCard> findAllByStatus_NameIgnoreCase(String statusName, Pageable pageable);

    @Query("SELECT ac FROM AnimalCard ac WHERE " +
           "(:statuses IS NULL OR ac.status.name IN :statuses) AND " +
           "(:goals IS NULL OR ac.goal.goal IN :goals) AND " +
           "(:createdAt IS NULL OR DATE(ac.created) = :createdAt) AND " +
           "(:updatedAt IS NULL OR DATE(ac.updated) = :updatedAt)")
    Page<AnimalCard> findByFilters(@Param("statuses") java.util.List<String> statuses,
                                   @Param("goals") java.util.List<String> goals,
                                   @Param("createdAt") LocalDate createdAt,
                                   @Param("updatedAt") LocalDate updatedAt,
                                   Pageable pageable);
}
