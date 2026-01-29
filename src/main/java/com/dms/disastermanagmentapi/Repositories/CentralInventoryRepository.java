package com.dms.disastermanagmentapi.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dms.disastermanagmentapi.entities.CentralInventory;

@Repository
public interface CentralInventoryRepository extends JpaRepository<CentralInventory, Integer> {
    Optional<CentralInventory> findByItemName(String itemName);
}