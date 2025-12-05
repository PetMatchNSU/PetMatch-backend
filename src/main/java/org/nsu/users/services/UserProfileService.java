package org.nsu.users.services;

import lombok.RequiredArgsConstructor;
import org.nsu.users.core.repositories.ContactTypeRepository;
import org.nsu.users.core.repositories.UserRepository;
import org.nsu.users.dto.requests.UpdateUserRequest;
import org.nsu.users.dto.responses.UserProfileResponse;
import org.nsu.users.entity.*;
import org.nsu.users.exceptions.ProfileNotFoundException;
import org.nsu.users.exceptions.RegionNotFoundException;
import org.nsu.users.exceptions.ContactTypeNotFoundException;
import org.nsu.users.mappers.BondTimeMapper;
import org.nsu.users.mappers.ContactMapper;
import org.nsu.users.repositories.RegionRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final ContactTypeRepository contactTypeRepository;
    private final BondTimeMapper bondTimeMapper;
    private final ContactMapper contactMapper;

    public UserProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ProfileNotFoundException(email));

        String fullName = buildFullName(user.getSecondName(), user.getFirstName(), user.getMiddleName());

        List<UserProfileResponse.BondTimeDto> bondTimeDtos = user.getBondTimes().stream()
                .map(bt -> new UserProfileResponse.BondTimeDto(bt.getStartContactTime(), bt.getEndContactTime()))
                .collect(Collectors.toList());

        List<UserProfileResponse.ContactInfoDto> contactDtos = user.getContacts().stream()
                .map(c -> new UserProfileResponse.ContactInfoDto(c.getType().getName(), c.getLink(), c.getIsVisible()))
                .collect(Collectors.toList());

        return UserProfileResponse.builder()
                .fullName(fullName)
                .gender(user.getGender().name())
                .region(user.getRegion().getRegion())
                .city(user.getRegion().getCity())
                .bondTime(bondTimeDtos)
                .contactInfo(contactDtos)
                .reviewStatus(user.getStatus().getName())
                .reviewComment(null)
                .build();
    }

    private String buildFullName(String secondName, String firstName, String middleName) {
        StringBuilder sb = new StringBuilder();
        sb.append(secondName).append(" ").append(firstName);
        if (StringUtils.hasText(middleName)) {
            sb.append(" ").append(middleName);
        }
        return sb.toString();
    }

    public void updateProfile(String email, UpdateUserRequest dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ProfileNotFoundException(email));

        user.setFirstName(dto.getFirstName());
        user.setSecondName(dto.getSecondName());
        user.setMiddleName(dto.getMiddleName());
        if (dto.getGender() != null) {
            user.setGender(Gender.valueOf(dto.getGender().name()));
        }

        Region region = regionRepository.findByRegionAndCity(dto.getRegion(), dto.getCity())
                .orElseThrow(() -> new RegionNotFoundException(dto.getRegion(), dto.getCity()));
        user.setRegion(region);

        Map<String, ContactType> typesMap = contactTypeRepository.findAll()
                .stream()
                .collect(Collectors.toMap(ContactType::getName, Function.identity()));

        user.getBondTimes().clear();
        if (!CollectionUtils.isEmpty(dto.getBondTime())) {
            List<BondTime> bondEntities = dto.getBondTime().stream()
                    .map(b -> {
                        BondTime bt = bondTimeMapper.toEntity(b);
                        bt.setUser(user);
                        return bt;
                    })
                    .collect(Collectors.toList());
            user.getBondTimes().addAll(bondEntities);
        }

        user.getContacts().clear();
        if (!CollectionUtils.isEmpty(dto.getContactInfo())) {
            List<Contact> contactEntities = dto.getContactInfo().stream()
                    .map(cdto -> {
                        ContactType ct = typesMap.get(cdto.getType());
                        if (ct == null) throw new ContactTypeNotFoundException(cdto.getType());

                        Contact contact = contactMapper.toEntity(cdto);
                        contact.setUser(user);
                        contact.setType(ct);
                        return contact;
                    })
                    .collect(Collectors.toList());
            user.getContacts().addAll(contactEntities);
        }

        userRepository.save(user);
    }

}
