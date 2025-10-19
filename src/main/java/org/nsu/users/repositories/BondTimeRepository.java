package org.nsu.users.repositories;

import org.nsu.users.entity.BondTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BondTimeRepository extends JpaRepository<BondTime, Long> {
    List<BondTime> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}