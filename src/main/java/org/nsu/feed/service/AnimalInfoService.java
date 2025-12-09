package org.nsu.feed.service;

import lombok.RequiredArgsConstructor;
import org.nsu.animal.repository.AnimalRepository;
import org.nsu.animal.repository.PlacementGoalRepository;
import org.nsu.feed.dto.responses.AnimalInfoResponse;
import org.nsu.feed.mappers.AnimalInfoMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnimalInfoService {
    private final AnimalRepository animalRepository;
    private final PlacementGoalRepository placementGoalRepository;
    private final AnimalInfoMapper animalInfoMapper;

    public AnimalInfoResponse getAnimalInfo() {
        AnimalInfoResponse response = new AnimalInfoResponse();

        var species = animalInfoMapper.toSpeciesDtoList(animalRepository.findAll());
        var goals = animalInfoMapper.toGoalDtoList(placementGoalRepository.findAll());

        response.setSpecies(species);
        response.setGoals(goals);

        return response;
    }
}
