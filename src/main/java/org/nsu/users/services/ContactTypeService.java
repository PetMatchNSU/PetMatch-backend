package org.nsu.users.services;

import lombok.RequiredArgsConstructor;
import org.nsu.users.dto.responses.ContactTypeResponse;
import org.nsu.users.entity.ContactType;
import org.nsu.users.mappers.ContactTypeMapper;
import org.nsu.users.repositories.ContactTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactTypeService {

    private final ContactTypeRepository contactTypeRepository;
    private final ContactTypeMapper contactTypeMapper;

    public ContactTypeResponse getAllContactTypes() {
        List<ContactType> contactTypes = contactTypeRepository.findAll();
        List<ContactTypeResponse.ContactTypeDto> dtos = contactTypeMapper.toContactTypeDtoList(contactTypes);
        
        return new ContactTypeResponse(dtos);
    }
}