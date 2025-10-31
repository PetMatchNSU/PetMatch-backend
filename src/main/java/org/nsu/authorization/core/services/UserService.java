package org.nsu.authorization.core.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;

import org.nsu.authorization.core.dto.requests.registrationRequest.RegistrationRequest;
import org.nsu.authorization.core.exceptions.authorization.UserCreationFailException;
import org.nsu.users.entity.Authority;
import org.nsu.users.entity.Gender;
import org.nsu.users.entity.User;
import org.nsu.authorization.core.repositories.AuthorityRepository;
import org.nsu.authorization.core.repositories.RegionRepository;
import org.nsu.authorization.core.repositories.StatusRepository;
import org.nsu.authorization.core.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final PasswordEncoder passwordEncoder;
    private final StatusRepository statusRepository;
    private final AuthorityRepository authorityRepository;

    @Transactional
    public User AddNewUser(RegistrationRequest request) {
        User newUser = mapRequestToUserEntity(request);
        Authority defaultAuthority = authorityRepository.findByName("Regular")
                .orElseThrow(() -> new UserCreationFailException(
                        "Default authority ('Regular') is missing from the database"));

        newUser.setAuthorities(new HashSet<>(Collections.singletonList(defaultAuthority)));

        User savedUser = userRepository.save(newUser);
        return savedUser;
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private User mapRequestToUserEntity(RegistrationRequest request) {
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User();

        user.setPassword(hashedPassword);
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setSecondName(request.getSecondName());
        user.setLastName(request.getLastName());
        user.setGender((request.getGender().equals("M")) ? Gender.M : Gender.F);
        user.setEmailVerified(false);

        user.setRegion(regionRepository.findByRegion(request.getRegion())
                .orElseThrow(() -> new UserCreationFailException("Failed to find region in the database")));

        user.setStatus(statusRepository.findByName("Active")
                .orElseThrow(
                        () -> new UserCreationFailException(
                                "Default user status ('Active') is missing from the database")));

        return user;
    }
}
