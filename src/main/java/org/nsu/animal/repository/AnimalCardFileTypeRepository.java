package org.nsu.animal.repository;

import org.nsu.animal.entity.AnimalCardFileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnimalCardFileTypeRepository extends JpaRepository<AnimalCardFileType, Long> {
    AnimalCardFileType findByName(String name);
}
