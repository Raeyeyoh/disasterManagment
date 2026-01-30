package com.dms.disastermanagmentapi.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dms.disastermanagmentapi.entities.IncidentAcknowledgement;
import com.dms.disastermanagmentapi.entities.IncidentReport;
import com.dms.disastermanagmentapi.entities.User;
import com.dms.disastermanagmentapi.enums.IncidentStatus;

@Repository
public interface AcknowledgementRepository extends JpaRepository<IncidentAcknowledgement, Long> {
    Optional<IncidentAcknowledgement> findByIncidentAndUser(IncidentReport incident, User user);
    
    List<IncidentAcknowledgement> findByIncident_ReportId(Integer incidentId);

    List<IncidentAcknowledgement> findAllByOrderByAcknowledgedAtDesc();
    List<IncidentAcknowledgement> findByIncident_Region_RegionIdOrderByAcknowledgedAtDesc(Integer regionId);
    List<IncidentAcknowledgement> findByIncident_Region_RegionIdAndIncident_StatusOrderByAcknowledgedAtDesc(
    Integer regionId, 
    IncidentStatus status
);
}