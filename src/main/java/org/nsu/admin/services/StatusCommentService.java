package org.nsu.admin.services;

import lombok.RequiredArgsConstructor;
import org.nsu.admin.entity.StatusComment;
import org.nsu.admin.repositories.StatusCommentRepository;
import org.nsu.users.entity.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StatusCommentService {

    private final StatusCommentRepository statusCommentRepository;

    public Optional<StatusComment> getLatestCommentByUser(User user) {
        return statusCommentRepository.findLatestByUser(user);
    }
}
