package com.beanbrew.repository;

import com.beanbrew.entity.InventoryType;
import com.beanbrew.entity.RefillPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RefillPredictionRepository extends JpaRepository<RefillPrediction, Long> {
    List<RefillPrediction> findByMachineIdOrderByCreatedAtDesc(String machineId);
    Optional<RefillPrediction> findTopByMachineIdAndInventoryTypeOrderByCreatedAtDesc(String machineId, InventoryType inventoryType);
    List<RefillPrediction> findAllByOrderByCreatedAtDesc();
}
