package org.nsu.users.repositories;

import org.nsu.users.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {

    @Query("select r from Region r " +
           "where lower(r.region) like lower(concat('%', :query, '%')) " +
           "   or lower(r.city) like lower(concat('%', :query, '%'))")
    List<Region> searchByRegionOrCity(@Param("query") String query);

    Optional<Region> findByRegionAndCity(String region, String city);
}