package org.nsu.animal.repository;

import org.nsu.animal.entity.AnimalCardFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnimalCardFileRepository extends JpaRepository<AnimalCardFile, Long> {
    List<AnimalCardFile> findByAnimalCardId(Long animalCardId);
    List<AnimalCardFile> findByFileIdIn(List<Long> fileIds);
    List<AnimalCardFile> findByAnimalCardIdIn(List<Long> cardIds);
    List<AnimalCardFile> findByFileTypeName(String name);
    List<AnimalCardFile> findByAnimalCardIdInAndFileTypeName(List<Long> cardIds, String name);
}
