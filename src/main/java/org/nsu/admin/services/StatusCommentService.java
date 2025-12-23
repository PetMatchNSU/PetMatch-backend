package org.nsu.admin.services;

import lombok.RequiredArgsConstructor;
import org.nsu.admin.entity.StatusComment;
import org.nsu.admin.repositories.StatusCommentRepository;
import org.nsu.animal.entity.AnimalCard;
import org.nsu.users.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatusCommentService {

    private final StatusCommentRepository statusCommentRepository;

    public Optional<StatusComment> getLatestCommentByUser(User user) {
        return statusCommentRepository.findTopByUserOrderByDateDesc(user);
    }

    public Optional<StatusComment> getLatestCommentByAnimalCard(AnimalCard animalCard) {
        return statusCommentRepository.findTopByAnimalCardOrderByDateDesc(animalCard);
    }

    public Map<Long, String> getLatestCommentsByCardIds(List<Long> cardIds) {
        List<StatusComment> comments = statusCommentRepository.findLatestByAnimalCardIds(cardIds);

        return comments.stream()
                .collect(Collectors.toMap(
                        sc -> sc.getAnimalCard().getId(),
                        StatusComment::getComment,
                        (first, second) -> first));
    }

}
