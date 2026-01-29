package com.dms.disastermanagmentapi.Repositories;

import com.dms.disastermanagmentapi.entities.ResourceAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResourceAllocationRepository extends JpaRepository<ResourceAllocation, Integer> {
    
    List<ResourceAllocation> findByRegion_RegionId(Integer regionId);

    List<ResourceAllocation> findByItemName(String itemName);
    
List<ResourceAllocation> findByRequest_RequestId(Integer requestId);}