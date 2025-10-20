package org.nsu.admin.repositories;

import org.nsu.admin.entity.StatusComment;
import org.nsu.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatusCommentRepository extends JpaRepository<StatusComment, Long> {
    
    @Query("SELECT sc FROM StatusComment sc WHERE sc.user = :user ORDER BY sc.date DESC LIMIT 1")
    Optional<StatusComment> findLatestByUser(@Param("user") User user);
}
