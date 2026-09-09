package com.beanbrew.service;

import com.beanbrew.entity.InventoryStatus;
import com.beanbrew.entity.VendingMachine;
import com.beanbrew.repository.InventoryStatusRepository;
import com.beanbrew.repository.VendingMachineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryStatusRepository inventoryStatusRepository;
    private final VendingMachineRepository machineRepository;

    public List<InventoryStatus> getAllLatest() {
        return machineRepository.findAll().stream()
                .map(m -> snapshotFromMachine(m))
                .toList();
    }

    public InventoryStatus getForMachine(String machineId) {
        VendingMachine m = machineRepository.findById(machineId).orElse(null);
        return m == null ? null : snapshotFromMachine(m);
    }

    public List<InventoryStatus> getHistory(String machineId) {
        return inventoryStatusRepository.findByMachineIdOrderByUpdatedAtDesc(machineId);
    }

    public void recordSnapshot(String machineId, double coffeePowder, double milk, double water) {
        InventoryStatus status = InventoryStatus.builder()
                .machineId(machineId)
                .coffeePowderLevel(coffeePowder)
                .milkLevel(milk)
                .waterLevel(water)
                .build();
        inventoryStatusRepository.save(status);
    }

    private InventoryStatus snapshotFromMachine(VendingMachine m) {
        return InventoryStatus.builder()
                .machineId(m.getMachineId())
                .coffeePowderLevel(m.getCoffeePowderLevel())
                .milkLevel(m.getMilkLevel())
                .waterLevel(m.getWaterLevel())
                .updatedAt(m.getLastSeen())
                .build();
    }
}
