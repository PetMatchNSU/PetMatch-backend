package org.nsu.feed.service;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.nsu.animal.entity.Animal;
import org.nsu.animal.entity.PlacementGoal;
import org.nsu.animal.repository.AnimalRepository;
import org.nsu.animal.repository.PlacementGoalRepository;
import org.nsu.feed.dto.requests.animalList.Filter;
import org.nsu.feed.dto.responses.AnimalInfoResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnimalInfoService {
    private final AnimalRepository animalRepository;
    private final PlacementGoalRepository placementGoalRepository;

    public AnimalInfoResponse getAnimalInfo() {
        AnimalInfoResponse response = new AnimalInfoResponse();

        List<Animal> animals = animalRepository.findAll();
        List<Filter.Species> species = animals.stream()
                .map(a -> {
                    Filter.Species s = new Filter.Species();
                    s.setId(a.getId());
                    s.setName(a.getName());
                    return s;
                })
                .collect(Collectors.toList());

        List<PlacementGoal> goals = placementGoalRepository.findAll();
        List<AnimalInfoResponse.Goal> goalDtos = goals.stream()
                .map(g -> {
                    AnimalInfoResponse.Goal gd = new AnimalInfoResponse.Goal();
                    gd.setId(g.getId());
                    gd.setName(g.getGoal());
                    return gd;
                })
                .collect(Collectors.toList());

        response.setSpecies(species);
        response.setGoals(goalDtos);

        return response;
    }
}
