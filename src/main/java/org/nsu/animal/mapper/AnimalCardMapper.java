package org.nsu.animal.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.nsu.animal.dto.enums.PhotoType;
import org.nsu.animal.dto.requests.CreateAnimalCardRequest;
import org.nsu.animal.dto.requests.UpdateAnimalCardRequest;
import org.nsu.animal.dto.responses.AnimalCardResponse;
import org.nsu.animal.entity.AnimalCard;
import org.nsu.animal.entity.AnimalCardFile;
import org.nsu.animal.entity.AnimalGender;
import org.nsu.users.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class AnimalCardMapper {

    @Mapping(target = "canEdit", source = "animalCard", qualifiedByName = "mapCanEdit")
    @Mapping(target = "species", source = "animalCard.animal", qualifiedByName = "mapSpecies")
    @Mapping(target = "goal", source = "animalCard.goal.goal")
    @Mapping(target = "gender", expression = "java(org.nsu.animal.dto.enums.Gender.valueOf(animalCard.getGender().name()))")
    @Mapping(target = "birthday", source = "animalCard.birthdate")
    @Mapping(target = "reviewStatus", source = "animalCard.status.name")
    @Mapping(target = "photos", ignore = true)
    @Mapping(target = "documents", ignore = true)
    @Mapping(target = "createdAt", source = "animalCard.created")
    @Mapping(target = "updatedAt", source = "animalCard.updated")
    public abstract AnimalCardResponse toResponse(AnimalCard animalCard, @Context User currentUser, @Context List<AnimalCardFile> files);

    @AfterMapping
    protected void mapFilesData(@MappingTarget AnimalCardResponse response, AnimalCard animalCard, @Context List<AnimalCardFile> files) {
        response.setPhotos(mapPhotos(files));
        response.setDocuments(mapDocuments(files));
    }

    @Named("mapCanEdit")
    protected Boolean mapCanEdit(AnimalCard animalCard, @Context User currentUser) {
        return Objects.equals(animalCard.getCardAuthor().getId(), currentUser.getId());
    }

    @Named("mapSpecies")
    protected AnimalCardResponse.SpeciesDto mapSpecies(org.nsu.animal.entity.Animal animal) {
        return new AnimalCardResponse.SpeciesDto(animal.getId(), animal.getName());
    }



    protected AnimalCardResponse.PhotosDto mapPhotos(List<AnimalCardFile> files) {
        Long mainPhotoId = files.stream()
                .filter(file -> PhotoType.MAIN_PHOTO.name().equals(file.getFileType().getName()))
                .findFirst()
                .map(file -> file.getFile().getId())
                .orElse(null);
        
        List<Long> additionalIds = files.stream()
                .filter(file -> PhotoType.ADDITIONAL_PHOTO.name().equals(file.getFileType().getName()))
                .map(file -> file.getFile().getId())
                .collect(Collectors.toList());
        
        return new AnimalCardResponse.PhotosDto(mainPhotoId, additionalIds);
    }

    protected AnimalCardResponse.DocumentsDto mapDocuments(List<AnimalCardFile> files) {
        Long vetPassportId = findFileIdByType(files, PhotoType.VET_PASSPORT);
        Long pedigreeId = findFileIdByType(files, PhotoType.PEDIGREE);
        Long vetCertificatesId = findFileIdByType(files, PhotoType.VET_CERTIFICATE);
        Long diplomasId = findFileIdByType(files, PhotoType.DIPLOMA);
        Long otherDocumentsId = findFileIdByType(files, PhotoType.OTHER_DOCUMENT);
        
        return new AnimalCardResponse.DocumentsDto(
                vetPassportId, pedigreeId, vetCertificatesId, diplomasId, otherDocumentsId);
    }
    
    private Long findFileIdByType(List<AnimalCardFile> files, PhotoType fileType) {
        return files.stream()
                .filter(file -> fileType.name().equals(file.getFileType().getName()))
                .findFirst()
                .map(file -> file.getFile().getId())
                .orElse(null);
    }

    @Mapping(target = "gender", source = "gender", qualifiedByName = "mapGenderToAnimalGender")
    @Mapping(target = "birthdate", source = "birthday")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cardAuthor", ignore = true)
    @Mapping(target = "animal", ignore = true)
    @Mapping(target = "goal", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "status", ignore = true)
    public abstract void updateAnimalCardFromRequest(CreateAnimalCardRequest request, @MappingTarget AnimalCard animalCard);

    @Mapping(target = "gender", source = "gender", qualifiedByName = "mapGenderToAnimalGender")
    @Mapping(target = "birthdate", source = "birthday")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cardAuthor", ignore = true)
    @Mapping(target = "animal", ignore = true)
    @Mapping(target = "goal", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "status", ignore = true)
    public abstract void updateAnimalCardFromRequest(UpdateAnimalCardRequest request, @MappingTarget AnimalCard animalCard);

    @Named("mapGenderToAnimalGender")
    protected AnimalGender mapGenderToAnimalGender(org.nsu.animal.dto.enums.Gender gender) {
        return AnimalGender.valueOf(gender.name());
    }
}
