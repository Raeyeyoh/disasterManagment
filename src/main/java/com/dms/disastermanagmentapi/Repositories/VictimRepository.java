package com.dms.disastermanagmentapi.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dms.disastermanagmentapi.entities.Victim;

@Repository
public interface VictimRepository extends JpaRepository<Victim, Integer> {
    Optional<Victim> findByNationalId(String nationalId);
    List<Victim> findByRegion_RegionId(Integer regionId);
}
