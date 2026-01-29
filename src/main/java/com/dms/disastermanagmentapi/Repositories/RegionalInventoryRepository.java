package com.dms.disastermanagmentapi.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dms.disastermanagmentapi.entities.RegionalInventory;
import com.dms.disastermanagmentapi.entities.RegionalInventoryId;
@Repository
public interface RegionalInventoryRepository extends JpaRepository<RegionalInventory, RegionalInventoryId> {
    List<RegionalInventory> findById_RegionId(Integer regionId);
    Optional<RegionalInventory> findByRegion_RegionIdAndItemName(Integer regionId, String itemName);
}
