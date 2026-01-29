package com.dms.disastermanagmentapi.Services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import com.dms.disastermanagmentapi.enums.ItemType;
import com.dms.disastermanagmentapi.enums.PriorityLevel;

@Service
public class InventoryPriorityService {

    public PriorityLevel calculatePriority(
            int regionRisk,
            ItemType itemType,
            int quantity,
            Instant createdAt
    ) {
        int score = 0;

        score += regionRisk * 2;

        score += itemType.getWeight() * 3;

        if (quantity >= 300) score += 3;
        else if (quantity >= 150) score += 2;
        else score += 1;

        if (createdAt.isBefore(Instant.now().minus(48, ChronoUnit.HOURS))) {
            score += 2;
        }

        if (score >= 14) return PriorityLevel.HIGH;
        if (score >= 9) return PriorityLevel.MEDIUM;
        return PriorityLevel.LOW;
    }
}
