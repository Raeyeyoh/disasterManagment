package com.dms.disastermanagmentapi.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dms.disastermanagmentapi.entities.InventoryTransferRequest;
import com.dms.disastermanagmentapi.enums.RequestStatus;

@Repository
public interface InventoryTransferRequestRepository extends JpaRepository<InventoryTransferRequest, Integer> {
    
    List<InventoryTransferRequest> findByStatus(RequestStatus status);

    List<InventoryTransferRequest> findByRegion_RegionId(Integer regionId);
    List<InventoryTransferRequest> findByRegion_RegionIdAndStatus(Integer regionId, RequestStatus status);
    @Query("""
SELECT r.region.regionName, r.itemType, SUM(r.quantity)
FROM InventoryTransferRequest r
WHERE r.status = RequestStatus.APPROVED
GROUP BY r.region.regionName, r.itemType

    """)
    List<Object[]> totalQuantityApprovedRequestsByRegion();
}
