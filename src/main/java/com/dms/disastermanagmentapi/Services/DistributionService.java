package com.dms.disastermanagmentapi.Services;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dms.disastermanagmentapi.Repositories.AidDistributionRepository;
import com.dms.disastermanagmentapi.Repositories.RegionalInventoryRepository;
import com.dms.disastermanagmentapi.Repositories.VictimRepository;
import com.dms.disastermanagmentapi.entities.AidDistribution;
import com.dms.disastermanagmentapi.entities.RegionalInventory;
import com.dms.disastermanagmentapi.entities.User;
import com.dms.disastermanagmentapi.entities.Victim;

@Service
public class DistributionService {

    @Autowired
    private RegionalInventoryRepository regionalRepo;

    @Autowired
    private AidDistributionRepository distributionRepo;
      @Autowired
    private VictimRepository victimRepo;


    @Transactional
    public void distributeAid(Integer victimId, String itemName, Integer quantity, User staff) {
        Victim victim = victimRepo.findById(victimId).orElseThrow();
        Integer regionId = victim.getRegion().getRegionId();

        
        RegionalInventory stock = regionalRepo.findByRegion_RegionIdAndItemName(regionId, itemName)
                .orElseThrow(() -> new RuntimeException("This region has no stock of " + itemName));

        if (stock.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock in region. Available: " + stock.getQuantity());
        }

        stock.setQuantity(stock.getQuantity() - quantity);
        stock.setLastUpdated(Instant.now());
        regionalRepo.save(stock);

        AidDistribution record = new AidDistribution();
        record.setVictim(victim);
        record.setItemName(itemName);
        record.setQuantityGiven(quantity);
        record.setDistributedBy(staff);
        record.setDistributedAt(Instant.now());
        
        distributionRepo.save(record);
    }
}