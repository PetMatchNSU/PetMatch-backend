package org.nsu.users.services;

import lombok.RequiredArgsConstructor;
import org.nsu.authorization.core.repositories.UserRepository;
import org.nsu.users.dto.requests.UpdateUserRequest;
import org.nsu.users.entity.*;
import org.nsu.users.mappers.BondTimeMapper;
import org.nsu.users.mappers.ContactMapper;
import org.nsu.users.repositories.ContactTypeRepository;
import org.nsu.users.repositories.RegionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final ContactTypeRepository contactTypeRepository;
    private final BondTimeMapper bondTimeMapper;
    private final ContactMapper contactMapper;

    public void updateProfile(String email, UpdateUserRequest dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setFirstName(dto.getFirstName());
        user.setSecondName(dto.getSecondName());
        user.setLastName(dto.getLastName());
        user.setGender(Gender.valueOf(dto.getGender().name()));

        Region region = regionRepository.findById(dto.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Region not found: " + dto.getLocationId()));
        user.setRegion(region);

        Map<String, ContactType> typesMap = contactTypeRepository.findAll()
                .stream()
                .collect(Collectors.toMap(ContactType::getName, ct -> ct));

        user.getBondTimes().clear();
        if (dto.getBondTime() != null && !dto.getBondTime().isEmpty()) {
            List<BondTime> bondEntities = dto.getBondTime().stream()
                    .map(b -> {
                        BondTime bt = bondTimeMapper.toEntity(b);
                        bt.setUser(user);
                        return bt;
                    })
                    .collect(Collectors.toList());
            user.getBondTimes().addAll(bondEntities);
        }

        user.getContacts().clear();
        if (dto.getContactInfo() != null && !dto.getContactInfo().isEmpty()) {
            List<Contact> contactEntities = dto.getContactInfo().stream()
                    .map(cdto -> {
                        Contact contact = contactMapper.toEntity(cdto);
                        contact.setUser(user);
                        ContactType ct = typesMap.get(cdto.getType());
                        if (ct == null) throw new IllegalArgumentException("Unknown contact type: " + cdto.getType());
                        contact.setType(ct);
                        return contact;
                    })
                    .collect(Collectors.toList());
            user.getContacts().addAll(contactEntities);
        }

        saveUserAggregate(user);
    }

    @Transactional
    protected void saveUserAggregate(User user) {
        userRepository.save(user);
    }

}