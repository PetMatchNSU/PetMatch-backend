package org.nsu.authorization.core.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import javax.management.RuntimeErrorException;

import org.nsu.authorization.core.dto.requests.RegistrationRequest;
import org.nsu.authorization.core.exceptions.authorization.RegionNotFoundException;
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
        return userRepository.save(newUser);
    }

    private User mapRequestToUserEntity(RegistrationRequest request) {
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User();
        user.setId(0L);
        user.setPassword(hashedPassword);
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setSecondName(request.getSecondName());
        user.setLastName(request.getLastName());
        user.setGender((request.getGender().equals("M")) ? Gender.M : Gender.F);
        user.setRegion(regionRepository.findByRegion(request.getRegion())
                .orElseThrow(() -> new RegionNotFoundException("Failed to find region in the database")));
        try {
            user.setStatus(statusRepository.getReferenceById(0L));
        } catch (Exception e) {
            throw new RegionNotFoundException("Failed to get user status from db");
        }
        // HashSet<Authority> hashSet = new HashSet<>();
        // Optional<Authority> authority = authorityRepository.findByName("Regular");
        // if (!authority.isPresent()) {
        // throw new RegionNotFoundException("Failed to get user authority from db");
        // }
        // hashSet.add(authority.get());
        // user.setAuthorities(hashSet);
        user.setEmailVerified(false);
        return user;
    }
}
