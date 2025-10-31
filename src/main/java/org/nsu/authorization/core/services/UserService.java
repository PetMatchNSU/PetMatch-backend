package org.nsu.authorization.core.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.nsu.authorization.core.dto.requests.registrationRequest.RegistrationRequest;
import org.nsu.authorization.core.exceptions.authorization.UserCreationFailException;
import org.nsu.users.entity.Authority;
import org.nsu.users.entity.Gender;
import org.nsu.users.entity.User;
import org.nsu.users.entity.userAuthority.UserAuthority;
import org.nsu.authorization.core.repositories.AuthorityRepository;
import org.nsu.authorization.core.repositories.RegionRepository;
import org.nsu.authorization.core.repositories.StatusRepository;
import org.nsu.authorization.core.repositories.UserAuthorityRepository;
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
    private final UserAuthorityRepository userAuthorityRepository;

    @Transactional
    public User AddNewUser(RegistrationRequest request) {
        User newUser = mapRequestToUserEntity(request);
        User savedUser = userRepository.save(newUser);
        assignDefaultAuthority(savedUser);
        return savedUser;
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
                                "Default user status (\"Active\") is missing from the database")));

        return user;
    }

    private void assignDefaultAuthority(User user) {
        Authority defaultAuthority = authorityRepository.findByName("Regular")
                .orElseThrow(() -> new UserCreationFailException(
                        "Default authority \"Regular\" is missing from the database"));

        UserAuthority userAuthority = new UserAuthority();
        userAuthority.setUser(user);
        userAuthority.setAuthority(defaultAuthority);

        userAuthorityRepository.save(userAuthority);
    }
}
