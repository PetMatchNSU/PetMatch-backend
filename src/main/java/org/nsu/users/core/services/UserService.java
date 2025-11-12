package org.nsu.users.core.services;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.nsu.admin.entity.StatusComment;
import org.nsu.admin.services.StatusCommentService;
import org.nsu.authorization.core.dto.requests.registrationRequest.RegistrationRequest;
import org.nsu.authorization.core.exceptions.authorization.PersonNotFoundException;
import org.nsu.authorization.core.security.PersonDetails;
import org.nsu.users.core.dto.responses.positive.UserResponse;
import org.nsu.users.core.mappers.UserMapper;
import org.nsu.users.core.repositories.UserRepository;
import org.nsu.users.entity.*;
import org.nsu.users.repositories.RegionRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.nsu.users.core.repositories.ContactTypeRepository;
import org.nsu.authorization.core.exceptions.authorization.UserCreationFailException;
import org.nsu.users.entity.User;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ContactService contactService;
    private final StatusCommentService statusCommentService;
    private final UserMapper userMapper;
    private final RegionRepository regionRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.nsu.users.core.repositories.StatusRepository statusRepository;
    private final ContactTypeRepository contactTypeRepository;

    public User addNewUser(RegistrationRequest request) {
        User newUser = mapRequestToUserEntity(request);

        newUser.setAuthorities(new HashSet<>());

        return userRepository.save(newUser);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private User mapRequestToUserEntity(RegistrationRequest request) {
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = userMapper.toUser(request);

        user.setPassword(hashedPassword);

        user.setRegion(regionRepository.findByRegion(request.getRegion())
                .orElseThrow(() -> new UserCreationFailException("Failed to find region in the database")));

        user.setStatus(statusRepository.findByName("ACTIVE")
                .orElseThrow(
                        () -> new UserCreationFailException(
                                "Default user status ('ACTIVE') is missing from the database")));

        List<BondTime> bondTimeEntities = request.getBondTime().stream()
                .map(bondTimeDto -> {
                    BondTime entity = new BondTime();
                    entity.setStartContactTime(bondTimeDto.getBondTimeStart());
                    entity.setEndContactTime(bondTimeDto.getBondTimeEnd());
                    entity.setUser(user); // back-reference
                    return entity;
                })
                .collect(Collectors.toList());

        user.setBondTimes(bondTimeEntities);

        List<Contact> contactsToSave = request.getContactInfo().stream()
                .map(contactInfo -> {
                    // Find the ContactType entity (e.g., "VK", "EMAIL")
                    String typeName = contactInfo.getType().name();
                    ContactType contactType = contactTypeRepository.findByName(typeName)
                            .orElseThrow(() -> new UserCreationFailException(
                                    "Contact type not found in database: " + typeName));

                    // Create the new Contact entity
                    Contact contact = new Contact();
                    contact.setUser(user); // Link to the user we just saved
                    contact.setType(contactType);
                    contact.setLink(contactInfo.getContact());
                    contact.setIsVisible(contactInfo.getVisible());
                    return contact;
                })
                .collect(Collectors.toList());

        user.setContacts(contactsToSave);

        return user;
    }

    public UserResponse getUserProfile() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();

        Long userId = personDetails.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new PersonNotFoundException("User not found"));

        Set<Contact> contacts = contactService.getContactsByUser(user);
        Optional<StatusComment> latestComment = statusCommentService.getLatestCommentByUser(user);
        String reviewComment = latestComment.map(StatusComment::getComment).orElse(null);

        return userMapper.toUserResponse(user, reviewComment, contacts);
    }
}
