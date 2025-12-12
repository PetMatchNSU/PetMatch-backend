package org.nsu.users.core.repositories;

import org.nsu.users.entity.User;
import org.nsu.users.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByStatusInAndEmailContainingIgnoreCase(List<Status> statuses, String emailPattern, Pageable pageable);

    Page<User> findByStatusIn(List<Status> statuses, Pageable pageable);

    Page<User> findByEmailContainingIgnoreCase(String emailPattern, Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
           "CASE WHEN :statuses IS NOT NULL THEN u.status.name IN :statuses ELSE TRUE END AND " +
           "(:emailToken IS NULL OR u.email LIKE CONCAT('%', :emailToken, '%'))")
    Page<User> findByFilters(@Param("statuses") List<String> statuses,
                             @Param("emailToken") String emailToken,
                             Pageable pageable);
}
