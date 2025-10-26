package org.nsu.users.core.services;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.nsu.admin.entity.StatusComment;
import org.nsu.admin.services.StatusCommentService;
import org.nsu.authorization.core.exceptions.authorization.PersonNotFoundException;
import org.nsu.users.core.dto.responses.positive.UserResponse;
import org.nsu.users.core.repositories.UserRepository;
import org.nsu.users.entity.Contact;
import org.nsu.users.entity.User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ContactService contactService;
    private final StatusCommentService statusCommentService;

    public UserResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new PersonNotFoundException("User not found"));

        Set<Contact> contacts = contactService.getContactsByUser(user);
        List<UserResponse.ContactInfoDto> contactInfoList = contacts.stream()
            .map(contact -> new UserResponse.ContactInfoDto(
                contact.getType().getName(),
                contact.getLink(),
                contact.getIsVisible()
            ))
            .collect(Collectors.toList());

        List<UserResponse.BondTimeDto> bondTimeList = user.getBondTimes().stream()
            .map(bondTime -> new UserResponse.BondTimeDto(
                convertLocalTimeToOffsetDateTime(bondTime.getStartContactTime()),
                convertLocalTimeToOffsetDateTime(bondTime.getEndContactTime())
            ))
            .collect(Collectors.toList());

        Optional<StatusComment> latestComment = statusCommentService.getLatestCommentByUser(user);
        String reviewComment = latestComment.map(StatusComment::getComment).orElse(null);

        return new UserResponse(
            user.getFirstName(),
            user.getSecondName(),
            user.getLastName(),
            user.getEmail(),
            user.getGender().name(),
            user.getRegion().getRegion(),
            user.getRegion().getCity(),
            user.getStatus().getName(),
            reviewComment,
            bondTimeList,
            contactInfoList
        );
    }

    private OffsetDateTime convertLocalTimeToOffsetDateTime(LocalTime localTime) {
        ZoneId moscowZone = ZoneId.of("Europe/Moscow");
        ZonedDateTime zdt = ZonedDateTime.now(moscowZone).with(localTime);
        return zdt.toOffsetDateTime();
    }
}
