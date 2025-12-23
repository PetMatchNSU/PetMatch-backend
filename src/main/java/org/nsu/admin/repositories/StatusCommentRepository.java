package org.nsu.admin.repositories;

import org.nsu.admin.entity.StatusComment;
import org.nsu.animal.entity.AnimalCard;
import org.nsu.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatusCommentRepository extends JpaRepository<StatusComment, Long> {
    
    Optional<StatusComment> findTopByUserOrderByDateDesc(User user);

    Optional<StatusComment> findTopByAnimalCardOrderByDateDesc(AnimalCard animalCard);
}
