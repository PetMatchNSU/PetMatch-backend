package org.nsu.users.repositories;

import org.nsu.users.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}