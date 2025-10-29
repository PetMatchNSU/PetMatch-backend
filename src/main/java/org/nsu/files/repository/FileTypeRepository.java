package org.nsu.files.repository;

import org.nsu.files.entity.FileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileTypeRepository extends JpaRepository<FileType, Long> {
    FileType findByName(String name);
}
