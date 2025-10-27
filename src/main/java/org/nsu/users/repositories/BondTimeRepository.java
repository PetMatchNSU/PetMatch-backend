package org.nsu.users.repositories;

import org.nsu.users.entity.BondTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BondTimeRepository extends JpaRepository<BondTime, Long> {
    List<BondTime> findByUserId(Long userId);
    
    @Modifying
    @Transactional
    void deleteByUserId(Long userId);
}