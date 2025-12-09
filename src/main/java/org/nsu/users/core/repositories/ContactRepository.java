package org.nsu.users.core.repositories;

import org.nsu.users.entity.Contact;
import org.nsu.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    Set<Contact> findByUser(User user);

    @Query("SELECT c FROM Contact c JOIN FETCH c.type WHERE c.user.id = :userId AND c.isVisible = true")
    List<Contact> findAllVisibleContactsByUserId(@Param("userId") Long userId);
}
