package org.nsu.users.core.services;

import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.nsu.admin.entity.StatusComment;
import org.nsu.admin.services.StatusCommentService;
import org.nsu.authorization.core.exceptions.authorization.PersonNotFoundException;
import org.nsu.authorization.core.security.PersonDetails;
import org.nsu.users.core.dto.responses.positive.UserResponse;
import org.nsu.users.core.mappers.UserMapper;
import org.nsu.users.core.repositories.UserRepository;
import org.nsu.users.entity.Contact;
import org.nsu.users.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ContactService contactService;
    private final StatusCommentService statusCommentService;
    private final UserMapper userMapper;

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
