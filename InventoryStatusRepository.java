package com.beanbrew.repository;

import com.beanbrew.entity.InventoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InventoryStatusRepository extends JpaRepository<InventoryStatus, Long> {
    List<InventoryStatus> findByMachineIdOrderByUpdatedAtDesc(String machineId);
    Optional<InventoryStatus> findTopByMachineIdOrderByUpdatedAtDesc(String machineId);
}
