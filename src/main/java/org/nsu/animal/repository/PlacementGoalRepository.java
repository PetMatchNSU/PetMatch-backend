package org.nsu.animal.repository;

import org.nsu.animal.entity.PlacementGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlacementGoalRepository extends JpaRepository<PlacementGoal, Long> {
}
