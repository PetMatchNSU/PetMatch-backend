package org.nsu.users.repositories;

import org.nsu.users.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RegionRepository extends JpaRepository<Region, Long> {

    @Query("select r from Region r " +
           "where lower(r.region) like lower(concat('%', :q, '%')) " +
           "   or lower(r.city) like lower(concat('%', :q, '%'))")
    List<Region> searchByRegionOrCity(@Param("q") String q);
}