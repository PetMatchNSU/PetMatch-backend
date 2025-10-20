package org.nsu.users.core.repositories;

import org.nsu.users.entity.Contact;
import org.nsu.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    
    Set<Contact> findByUser(User user);
}
