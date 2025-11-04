package org.nsu.authorization.core.services;

import lombok.RequiredArgsConstructor;
import org.nsu.users.entity.BondTime;
import org.nsu.users.mapping.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.nsu.authorization.core.dto.requests.registrationRequest.RegistrationRequest;
import org.nsu.authorization.core.exceptions.authorization.UserCreationFailException;
import org.nsu.users.entity.Authority;
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
    private final UserMapper userMapper;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Transactional
    public User AddNewUser(RegistrationRequest request) {
        User newUser = mapRequestToUserEntity(request);
        Authority defaultAuthority = authorityRepository.findByName("Regular")
                .orElseThrow(() -> new UserCreationFailException(
                        "Default authority ('Regular') is missing from the database"));

        newUser.setAuthorities(new HashSet<>(Collections.singletonList(defaultAuthority)));

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
                    entity.setStartContactTime(LocalTime.parse(bondTimeDto.getBondTimeStart(), TIME_FORMATTER));
                    entity.setEndContactTime(LocalTime.parse(bondTimeDto.getBondTimeEnd(), TIME_FORMATTER));
                    entity.setUser(user);
                    return entity;
                })
                .collect(Collectors.toList());

        user.setBondTimes(bondTimeEntities);

        return user;
    }
}
