package org.nsu.authorization.core.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.nsu.authorization.core.dto.requests.RegistrationRequest;
import org.nsu.authorization.core.exceptions.authorization.RegionNotFoundException;
import org.nsu.users.entity.Gender;
import org.nsu.users.entity.User;
import org.nsu.authorization.core.repositories.RegionRepository;
import org.nsu.authorization.core.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User AddNewUser(RegistrationRequest request) {
        User newUser = mapRequestToUserEntity(request);
        return newUser;
        // return userRepository.save(newUser);
    }

    private User mapRequestToUserEntity(RegistrationRequest request) {
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User();
        // user.setPassword(hashedPassword);
        // user.setEmail(request.getEmail());
        // user.setFirstName(request.getFirstName());
        // user.setSecondName(request.getSecondName());
        // user.setLastName(request.getLastName());
        // user.setGender((request.getGender().equals("M")) ? Gender.M : Gender.F);
        // user.setRegion(regionRepository.findByRegion(request.getRegion())
        // .orElseThrow(() -> new RegionNotFoundException("Failed to find region in the
        // database")));
        // user.setEmailVerified(false);
        return user;
    }
}
