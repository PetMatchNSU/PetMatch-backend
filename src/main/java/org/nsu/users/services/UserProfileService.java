package org.nsu.users.services;

import lombok.RequiredArgsConstructor;
import org.nsu.authorization.core.repositories.UserRepository;
import org.nsu.users.dto.requests.UpdateUserRequest;
import org.nsu.users.entity.*;
import org.nsu.users.repositories.BondTimeRepository;
import org.nsu.users.repositories.ContactRepository;
import org.nsu.users.repositories.ContactTypeRepository;
import org.nsu.users.repositories.RegionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final ContactRepository contactRepository;
    private final ContactTypeRepository contactTypeRepository;

    public void updateProfile(String email, UpdateUserRequest dto) {
        updateUserBasicInfo(email, dto);
        updateBondTimes(email, dto.getBondTime());
        updateContacts(email, dto.getContactInfo());
    }

    @Transactional
    protected void updateUserBasicInfo(String email, UpdateUserRequest dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setFirstName(dto.getFirstName());
        user.setSecondName(dto.getSecondName());
        user.setLastName(dto.getLastName());
        user.setGender(Gender.valueOf(dto.getGender().name()));

        Region region = regionRepository.findById(dto.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Region not found: " + dto.getLocationId()));
        user.setRegion(region);

        userRepository.save(user);
    }

    @Transactional
    protected void updateBondTimes(String email, List<UpdateUserRequest.BondTimeDto> bondTimeDtos) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

      
        user.getBondTimes().clear();
        if (bondTimeDtos == null || bondTimeDtos.isEmpty()) {
            userRepository.save(user);
            return;
        }

        for (UpdateUserRequest.BondTimeDto b : bondTimeDtos) {
            BondTime bt = new BondTime();
            bt.setStart(b.getBondTimeStart());
            bt.setEnd(b.getBondTimeEnd());
            bt.setUser(user);
            user.getBondTimes().add(bt);
        }

        userRepository.save(user);
    }

    @Transactional
    protected void updateContacts(String email, List<UpdateUserRequest.ContactInfoDto> contactDtos) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        contactRepository.deleteByUserId(user.getId());
        
        if (contactDtos == null || contactDtos.isEmpty()) {
            return;
        }

        List<Contact> toSave = new ArrayList<>();
        for (UpdateUserRequest.ContactInfoDto c : contactDtos) {
            ContactType type = contactTypeRepository.findByName(c.getType())
                .orElseThrow(() -> new IllegalArgumentException("Unknown contact type: " + c.getType()));
            Contact contact = new Contact();
            contact.setType(type);
            contact.setLink(c.getContact());
            contact.setUser(user);
            contact.setIsVisible(c.getVisible());
            toSave.add(contact);
        }
        contactRepository.saveAll(toSave);
    }
}