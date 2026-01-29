package com.dms.disastermanagmentapi.Repositories;


import com.dms.disastermanagmentapi.enums.IncidentStatus;
import com.dms.disastermanagmentapi.enums.IncidentTitle;
import com.dms.disastermanagmentapi.enums.SeverityLevel;
import com.dms.disastermanagmentapi.entities.IncidentReport;
import com.dms.disastermanagmentapi.entities.Region;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest 
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class IncidentRepositoryTest {

    @Autowired
    private IncidentReportRepository repository;
@Autowired
private RegionRepository regionRepository;
   @Test
void testFindPendingByRegion() {
    // 1. Create the Region first (The Parent)
    Region region = new Region();
    region.setRegionName("AddisAbaba");
    region.setRiskLevel(5);
    // Save it so it gets an ID
    region = regionRepository.save(region); 

    IncidentReport report = new IncidentReport();
    report.setTitle(IncidentTitle.FLOOD); 
    report.setSeverity(SeverityLevel.HIGH);
    report.setStatus(IncidentStatus.PENDING);
    report.setDescription("Severe flooding in downtown area.");
    report.setRegion(region); // Link to the saved region
    report.setCreatedAt(Instant.now());
    // lastNotifiedAt is left NULL (which is fine, it's nullable!)
    
    repository.save(report);

    // 3. Act
    List<IncidentReport> results = repository.findByRegion_RegionIdAndStatus(
        region.getRegionId(), 
        IncidentStatus.PENDING
    );

    // 4. Assert
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
}
}