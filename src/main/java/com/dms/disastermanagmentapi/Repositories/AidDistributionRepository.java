package com.dms.disastermanagmentapi.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dms.disastermanagmentapi.entities.AidDistribution;
import com.dms.disastermanagmentapi.entities.Region;

@Repository
public interface AidDistributionRepository extends JpaRepository<AidDistribution, Integer> {
    List<AidDistribution> findByIncidentReportId(Integer reportId);
    List<AidDistribution> findByVictim_VictimId(Integer victimId);
    
    @Query("""
        SELECT d.itemName, SUM(d.quantityGiven)
        FROM AidDistribution d
        GROUP BY d.itemName
    """)
    List<Object[]> getTotalAidByItem();
     @Query("""
        SELECT i.region.regionName, d.itemName, SUM(d.quantityGiven)
        FROM AidDistribution d
        JOIN d.incident i
        GROUP BY i.region.regionName, d.itemName
    """)
    List<Object[]> getTotalAidByItemByRegion();
    @Query("""
    SELECT d.incident.region.regionName, d.itemName, SUM(d.quantityGiven)
    FROM AidDistribution d
    WHERE d.incident.region = :region
    GROUP BY d.incident.region.regionName, d.itemName
""")
List<Object[]> getTotalAidByItemByRegionFiltered(@Param("region") Region region);

}
