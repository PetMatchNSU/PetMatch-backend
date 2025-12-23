package org.nsu.admin.repositories;

import org.nsu.admin.entity.StatusComment;
import org.nsu.animal.entity.AnimalCard;
import org.nsu.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StatusCommentRepository extends JpaRepository<StatusComment, Long> {

    Optional<StatusComment> findTopByUserOrderByDateDesc(User user);

    Optional<StatusComment> findTopByAnimalCardOrderByDateDesc(AnimalCard animalCard);

    @Query("SELECT sc FROM StatusComment sc " +
            "WHERE sc.animalCard.id IN :cardIds " +
            "ORDER BY sc.animalCard.id, sc.date DESC")
    List<StatusComment> findLatestByAnimalCardIds(@Param("cardIds") List<Long> cardIds);

}
