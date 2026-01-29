package com.dms.disastermanagmentapi.Repositories;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.dms.disastermanagmentapi.entities.IncidentReport;
import com.dms.disastermanagmentapi.enums.IncidentStatus;
@Repository
public interface IncidentReportRepository extends JpaRepository<IncidentReport, Integer> {
    
    List<IncidentReport> findByRegion_RegionId(Integer regionId);

    List<IncidentReport> findByStatus(IncidentStatus status);
    List<IncidentReport> findByStatusAndCreatedAtBefore(IncidentStatus status, Instant before);
    List<IncidentReport> findByRegion_RegionIdAndStatus(Integer regionId, IncidentStatus status);

}
