package com.dms.disastermanagmentapi.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dms.disastermanagmentapi.entities.AidDistribution;

@Repository
public interface AidDistributionRepository extends JpaRepository<AidDistribution, Integer> {
    List<AidDistribution> findByIncidentReportId(Integer reportId);
    List<AidDistribution> findByVictim_VictimId(Integer victimId);
}
