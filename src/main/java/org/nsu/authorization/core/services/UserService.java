package org.nsu.authorization.core.services;

import lombok.RequiredArgsConstructor;
import org.nsu.users.entity.BondTime;
import org.nsu.users.mapping.UserMapper;
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
import org.nsu.authorization.core.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final PasswordEncoder passwordEncoder;
    private final StatusRepository statusRepository;
    private final UserMapper userMapper;

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
}
