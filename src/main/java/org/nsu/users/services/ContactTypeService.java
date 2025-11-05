package org.nsu.users.services;

import lombok.RequiredArgsConstructor;
import org.nsu.users.dto.responses.ContactTypeResponse;
import org.nsu.users.entity.ContactType;
import org.nsu.users.mappers.ContactTypeMapper;
import org.nsu.users.repositories.ContactTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContactTypeService {

    private final ContactTypeRepository contactTypeRepository;
    private final ContactTypeMapper contactTypeMapper;

    public ContactTypeResponse getAllContactTypes() {
        return Optional.ofNullable(contactTypeRepository.findAll())
                .map(contactTypeMapper::toContactTypeDtoList)
                .map(ContactTypeResponse::new)
                .filter(r -> r.getContactTypes() != null && !r.getContactTypes().isEmpty())
                .orElse(ContactTypeResponse.EMPTY);
    }
}