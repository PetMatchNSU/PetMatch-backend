package org.nsu.users.core.services;

import lombok.RequiredArgsConstructor;
import org.nsu.users.core.repositories.ContactRepository;
import org.nsu.users.entity.Contact;
import org.nsu.users.entity.User;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;

    public Set<Contact> getContactsByUser(User user) {
        return contactRepository.findByUser(user);
    }
}
