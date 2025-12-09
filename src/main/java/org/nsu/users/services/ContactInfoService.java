package org.nsu.users.services;

import lombok.RequiredArgsConstructor;

import org.nsu.users.core.repositories.ContactRepository;
import org.nsu.users.core.repositories.ContactTypeRepository;
import org.nsu.users.dto.responses.ContactInfoResponse;
import org.nsu.users.mappers.ContactInfoMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContactInfoService {

    private final ContactInfoMapper contactInfoMapper;
    private final ContactRepository contactRepository;

    public ContactInfoResponse getContactInfo(Long userId) {
        return Optional.ofNullable(contactRepository.findAllVisibleContactsByUserId(userId))
                .map(contactInfoMapper::toContactInfoDtoList)
                .map(ContactInfoResponse::new)
                .filter(r -> r.getContactInfo() != null && !r.getContactInfo().isEmpty())
                .orElse(ContactInfoResponse.EMPTY);
    }
}