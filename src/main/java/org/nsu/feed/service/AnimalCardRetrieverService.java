package org.nsu.feed.service;

import lombok.RequiredArgsConstructor;
import org.nsu.animal.entity.AnimalCard;
import org.nsu.animal.entity.AnimalCardFile;
import org.nsu.animal.repository.AnimalCardFileRepository;
import org.nsu.animal.repository.AnimalCardRepository;
import org.nsu.feed.dto.requests.animalList.Filter;
import org.nsu.feed.dto.util.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnimalCardRetrieverService {
    private final AnimalCardRepository animalCardRepository;
    private final AnimalCardFileRepository animalCardFileRepository;

    public List<org.nsu.feed.dto.responses.animalList.AnimalCard> getListNoFilters(long page, long limit) {
        // delegate to specification-based listing without filter
        Page<org.nsu.feed.dto.responses.animalList.AnimalCard> pageResult = getList(null, page, limit);
        return pageResult.getContent();
    }

    public Page<org.nsu.feed.dto.responses.animalList.AnimalCard> getList(Filter filter, long page, long pageLimit) {
        final Long fUserId = filter != null ? filter.getUserId() : null;
        final Filter.Species fSpecies = filter != null ? filter.getSpecies() : null;
        final List<Filter.Breed> fBreeds = filter != null ? filter.getBreeds() : null;
        final Boolean fHadBreed = filter != null ? filter.getHadBreed() : null;
        final org.nsu.animal.entity.AnimalGender fGender = filter != null ? filter.getGender() : null;
        final List<String> fGoals = filter != null ? filter.getGoals() : null;
        final org.nsu.feed.dto.util.Location fLocation = filter != null ? filter.getLocation() : null;
        final Boolean fVetPassport = filter != null ? filter.getVetPassport() : null;
        final Boolean fPedigree = filter != null ? filter.getPedigree() : null;

        Specification<AnimalCard> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();

            // if userId provided - show all statuses for that user; otherwise only PUBLISHED
            if (fUserId != null) {
                preds.add(cb.equal(root.get("cardAuthor").get("id"), fUserId));
            } else {
                preds.add(cb.equal(cb.lower(root.get("status").get("name")), "published"));
            }

            if (fSpecies != null) {
                if (fSpecies.getId() != null) {
                    preds.add(cb.equal(root.get("animal").get("id"), fSpecies.getId()));
                } else if (fSpecies.getName() != null) {
                    preds.add(cb.like(cb.lower(root.get("animal").get("name")), "%" + fSpecies.getName().toLowerCase() + "%"));
                }
            }

            if (fBreeds != null && !fBreeds.isEmpty()) {
                List<Predicate> breedPreds = new ArrayList<>();
                for (var b : fBreeds) {
                    if (b.getBreedName() != null) {
                        breedPreds.add(cb.equal(cb.lower(root.get("breed")), b.getBreedName().toLowerCase()));
                    }
                }
                if (!breedPreds.isEmpty()) preds.add(cb.or(breedPreds.toArray(new Predicate[0])));
            } else {
                if (fHadBreed != null) {
                    if (fHadBreed) {
                        preds.add(cb.and(cb.isNotNull(root.get("breed")), cb.notEqual(cb.trim(root.get("breed")), "")));
                    } else {
                        preds.add(cb.or(cb.isNull(root.get("breed")), cb.equal(cb.trim(root.get("breed")), "")));
                    }
                }
            }

            if (fGender != null) {
                preds.add(cb.equal(root.get("gender"), fGender));
            }

            if (fGoals != null && !fGoals.isEmpty()) {
                Join<Object, Object> goalJoin = root.join("goal", JoinType.LEFT);
                List<Predicate> goalPreds = fGoals.stream()
                    .map(g -> cb.equal(cb.lower(goalJoin.get("goal")), g.toLowerCase()))
                    .collect(Collectors.toList());
                preds.add(cb.or(goalPreds.toArray(new Predicate[0])));
            }

            if (fLocation != null) {
                if (fLocation.getRegion() != null) {
                    preds.add(cb.equal(cb.lower(root.get("cardAuthor").get("region").get("region")), fLocation.getRegion().toLowerCase()));
                }
                if (fLocation.getCity() != null) {
                    preds.add(cb.equal(cb.lower(root.get("cardAuthor").get("region").get("city")), fLocation.getCity().toLowerCase()));
                }
            }

            // vetPassport: check existence of an AnimalCardFile with fileType.name = 'vet_passport'
            if (fVetPassport != null) {
                Subquery<Long> sq = query.subquery(Long.class);
                jakarta.persistence.criteria.Root<org.nsu.animal.entity.AnimalCardFile> acfRoot = sq.from(org.nsu.animal.entity.AnimalCardFile.class);
                sq.select(acfRoot.get("id"));
                sq.where(
                    cb.equal(acfRoot.get("animalCard").get("id"), root.get("id")),
                    cb.equal(cb.lower(acfRoot.get("fileType").get("name")), "vet_passport")
                );
                if (fVetPassport) {
                    preds.add(cb.exists(sq));
                } else {
                    preds.add(cb.not(cb.exists(sq)));
                }
            }

            // pedigree: check existence of an AnimalCardFile with fileType.name = 'pedigree'
            if (fPedigree != null) {
                Subquery<Long> sq2 = query.subquery(Long.class);
                jakarta.persistence.criteria.Root<org.nsu.animal.entity.AnimalCardFile> acfRoot2 = sq2.from(org.nsu.animal.entity.AnimalCardFile.class);
                sq2.select(acfRoot2.get("id"));
                sq2.where(
                    cb.equal(acfRoot2.get("animalCard").get("id"), root.get("id")),
                    cb.equal(cb.lower(acfRoot2.get("fileType").get("name")), "pedigree")
                );
                if (fPedigree) {
                    preds.add(cb.exists(sq2));
                } else {
                    preds.add(cb.not(cb.exists(sq2)));
                }
            }

            query.distinct(true);
            return cb.and(preds.toArray(new Predicate[0]));
        };

        int p = (int) Math.max(1, page) - 1;
        int l = (int) Math.max(1, pageLimit);
        Pageable pageable = PageRequest.of(p, l, Sort.by(Sort.Direction.DESC, "created"));

        Page<AnimalCard> cards = animalCardRepository.findAll(spec, pageable);

        List<org.nsu.feed.dto.responses.animalList.AnimalCard> dtoList = cards.getContent().stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, cards.getTotalElements());
    }

    private org.nsu.feed.dto.responses.animalList.AnimalCard mapToDto(AnimalCard ac) {
        org.nsu.feed.dto.responses.animalList.AnimalCard dto = new org.nsu.feed.dto.responses.animalList.AnimalCard();
        dto.setAnimalId(ac.getId());
        dto.setName(ac.getName());
        if (ac.getAnimal() != null) dto.setSpeciesName(ac.getAnimal().getName());
        dto.setGoal(ac.getGoal() != null ? ac.getGoal().getGoal() : null);
        dto.setHasBreed(ac.getBreed() != null && !ac.getBreed().isBlank());
        dto.setBreed(ac.getBreed());
        dto.setGender(ac.getGender());
        dto.setBirthday(ac.getBirthdate());

        Location loc = new Location();
        if (ac.getCardAuthor() != null && ac.getCardAuthor().getRegion() != null) {
            if (ac.getCardAuthor().getRegion().getCity() != null) {
                loc.setCity(ac.getCardAuthor().getRegion().getCity());
            }
            if (ac.getCardAuthor().getRegion().getRegion() != null) {
                loc.setRegion(ac.getCardAuthor().getRegion().getRegion());
            }
        }
        dto.setLocation(loc);

        // try to find main photo (file type name 'photo')
        List<AnimalCardFile> files = animalCardFileRepository.findByAnimalCardId(ac.getId());
        files.stream()
            .filter(f -> f.getFileType() != null && "photo".equalsIgnoreCase(f.getFileType().getName()))
            .findFirst()
            .ifPresent(f -> dto.setMainPhotoId(f.getFile() != null ? f.getFile().getId() : null));

        if (ac.getCreated() != null) dto.setCreatedAt(ac.getCreated().atZone(ZoneOffset.UTC));

        return dto;
    }
}
