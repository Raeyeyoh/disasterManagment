package com.dms.disastermanagmentapi.Services;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dms.disastermanagmentapi.Repositories.CentralInventoryRepository;
import com.dms.disastermanagmentapi.Repositories.InventoryTransferRequestRepository;
import com.dms.disastermanagmentapi.Repositories.RegionalInventoryRepository;
import com.dms.disastermanagmentapi.Repositories.ResourceAllocationRepository;
import com.dms.disastermanagmentapi.entities.CentralInventory;
import com.dms.disastermanagmentapi.entities.InventoryTransferRequest;
import com.dms.disastermanagmentapi.entities.RegionalInventory;
import com.dms.disastermanagmentapi.entities.RegionalInventoryId;
import com.dms.disastermanagmentapi.entities.ResourceAllocation;
import com.dms.disastermanagmentapi.entities.User;
import com.dms.disastermanagmentapi.enums.ItemType;
import com.dms.disastermanagmentapi.enums.RequestStatus;
import com.dms.disastermanagmentapi.enums.Unit;
@Service
public class InventoryService {
@Autowired
    private CentralInventoryRepository centralRepo;

    @Autowired
    private RegionalInventoryRepository regionalRepo;

    @Autowired
    private InventoryTransferRequestRepository transferRepo;
    @Autowired
private ResourceAllocationRepository allocationRepo;

    @Transactional
    public void approveTransfer(Integer requestId , User admin) {
        InventoryTransferRequest req = transferRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (req.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request is already processed");
        }

       CentralInventory centralItem = centralRepo.findByItemName(req.getItemName())
        .orElseThrow(() -> new RuntimeException("We don't have " + req.getItemName() + " in Central Stock."));

    if (centralItem.getQuantity() < req.getQuantity()) {
        throw new RuntimeException("Not enough stock in Central to fulfill this request.");
    }

        centralItem.setQuantity(centralItem.getQuantity() - req.getQuantity());
        centralRepo.save(centralItem);

RegionalInventoryId regionalId = new RegionalInventoryId(
        centralItem.getItem_Id(),     
        req.getRegion().getRegionId()  
);

RegionalInventory regionalItem = regionalRepo.findById(regionalId)
        .map(existing -> {
            existing.setQuantity(existing.getQuantity() + req.getQuantity());
            existing.setLastUpdated(Instant.now());
            return existing;
        })
        .orElseGet(() -> {
            RegionalInventory newItem = new RegionalInventory();
            newItem.setId(regionalId);
            newItem.setRegion(req.getRegion());
            newItem.setItemName(centralItem.getItemName());
            newItem.setType(centralItem.getType());
            newItem.setUnit(centralItem.getUnit());
            newItem.setQuantity(req.getQuantity());
            newItem.setLastUpdated(Instant.now());
            return newItem;
        });

regionalRepo.save(regionalItem);

ResourceAllocation allocation = new ResourceAllocation();
allocation.setRequest(req);
    allocation.setRegion(req.getRegion());
    allocation.setItemName(req.getItemName());
    allocation.setQuantity(req.getQuantity());
    allocation.setAllocatedBy(admin); 
    allocation.setAllocatedAt(Instant.now());
    
    allocationRepo.save(allocation);


        req.setStatus(RequestStatus.APPROVED);
        transferRepo.save(req);
    }
     public Unit getBaseUnit(ItemType type) {
    return switch (type) {
        case FOOD -> Unit.KG;
        case MEDICAL -> Unit.PIECE;
        case EQUIPMENT -> Unit.PIECE;
        case OTHER -> Unit.PIECE;
    };
}
}
