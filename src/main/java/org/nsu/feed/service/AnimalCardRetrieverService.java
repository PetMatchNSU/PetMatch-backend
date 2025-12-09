package org.nsu.feed.service;

import lombok.RequiredArgsConstructor;
import org.nsu.animal.entity.AnimalCard;
import org.nsu.animal.repository.AnimalCardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnimalCardRetrieverService {
    private final AnimalCardRepository animalCardRepository;
    private final org.nsu.feed.mappers.AnimalCardMapper animalCardMapper;

    public Page<org.nsu.feed.dto.responses.animalList.AnimalCardDto> getList(long page, long pageLimit) {
        int p = (int) Math.max(1, page) - 1;
        int l = (int) Math.max(1, pageLimit);
        Pageable pageable = PageRequest.of(p, l, Sort.by(Sort.Direction.DESC, "created"));

        Page<AnimalCard> cards = animalCardRepository.findAllByStatus_NameIgnoreCase("PUBLISHED", pageable);

        List<org.nsu.feed.dto.responses.animalList.AnimalCardDto> dtoList = cards.getContent().stream()
            .map(ac -> animalCardMapper.toDto(ac))
            .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, cards.getTotalElements());
    }
}
