package org.nsu.users.core.services;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.nsu.admin.entity.StatusComment;
import org.nsu.admin.services.StatusCommentService;
import org.nsu.authorization.core.dto.requests.registrationRequest.RegistrationRequest;
import org.nsu.authorization.core.exceptions.authorization.PersonNotFoundException;
import org.nsu.authorization.core.security.PersonDetails;
import org.nsu.users.core.dto.responses.positive.UserResponse;
import org.nsu.users.core.mappers.UserMapper;
import org.nsu.users.core.repositories.UserRepository;
import org.nsu.users.entity.Contact;
import org.nsu.users.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.nsu.users.entity.BondTime;
import org.nsu.users.core.mappers.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.nsu.authorization.core.dto.requests.registrationRequest.RegistrationRequest;
import org.nsu.authorization.core.exceptions.authorization.UserCreationFailException;
import org.nsu.users.entity.User;
import org.nsu.authorization.core.repositories.RegionRepository;
import org.nsu.authorization.core.repositories.StatusRepository;
import org.nsu.users.core.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ContactService contactService;
    private final StatusCommentService statusCommentService;
    private final UserMapper userMapper;
    private final org.nsu.authorization.core.repositories.RegionRepository regionRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.nsu.authorization.core.repositories.StatusRepository statusRepository;

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

        user.setStatus(statusRepository.findByName("Active")
                .orElseThrow(
                        () -> new UserCreationFailException(
                                "Default user status ('Active') is missing from the database")));

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
