package org.nsu.users.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.nsu.authorization.core.repositories.UserRepository;
import org.nsu.users.dto.requests.UpdateUserRequest;
import org.nsu.users.entity.*;
import org.nsu.users.repositories.BondTimeRepository;
import org.nsu.users.repositories.ContactRepository;
import org.nsu.users.repositories.ContactTypeRepository;
import org.nsu.users.repositories.RegionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final ContactRepository contactRepository;
    private final ContactTypeRepository contactTypeRepository;
    private final BondTimeRepository bondTimeRepository;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Transactional
    public void updateProfile(String email, UpdateUserRequest dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Update core fields
        user.setFirstName(dto.getFirstName());
        user.setSecondName(dto.getSecondName());
        user.setLastName(dto.getLastName());

        try {
            user.setGender(Gender.valueOf(dto.getGender().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Gender must be 'M' or 'F'");
        }

        Region region = regionRepository.findById(dto.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Region not found: " + dto.getLocationId()));
        user.setRegion(region);

        userRepository.save(user);

        // Replace BondTime set
        bondTimeRepository.deleteByUserId(user.getId());
        List<BondTime> toSaveBond = new ArrayList<>();
        if (dto.getBondTime() != null) {
            for (UpdateUserRequest.BondTimeDto b : dto.getBondTime()) {
                BondTime bt = new BondTime();
                bt.setUser(user);
                bt.setStart(LocalTime.parse(b.getBondTimeStart(), TIME_FMT));
                bt.setEnd(LocalTime.parse(b.getBondTimeEnd(), TIME_FMT));
                toSaveBond.add(bt);
            }
        }
        if (!toSaveBond.isEmpty()) {
            bondTimeRepository.saveAll(toSaveBond);
        }

        // Replace Contacts set
        contactRepository.deleteByUserId(user.getId());
        List<Contact> toSaveContacts = new ArrayList<>();
        if (dto.getContactInfo() != null) {
            for (UpdateUserRequest.ContactInfoDto c : dto.getContactInfo()) {
                ContactType type = contactTypeRepository.findByName(c.getType())
                        .orElseThrow(() -> new IllegalArgumentException("Unknown contact type: " + c.getType()));
                Contact contact = new Contact();
                contact.setType(type);
                contact.setLink(c.getContact());
                contact.setUser(user);
                contact.setIsVisible(c.getVisible());
                toSaveContacts.add(contact);
            }
        }
        if (!toSaveContacts.isEmpty()) {
            contactRepository.saveAll(toSaveContacts);
        }
    }
}