package org.nsu.users.core.repositories;

import org.nsu.users.entity.Contact;
import org.nsu.users.entity.ContactType;
import org.nsu.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface ContactTypeRepository extends JpaRepository<ContactType, Long> {
    Optional<ContactType> findByName(String name);
}
