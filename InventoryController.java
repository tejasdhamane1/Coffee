package com.beanbrew.controller;

import com.beanbrew.entity.InventoryStatus;
import com.beanbrew.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public List<InventoryStatus> getAll() {
        return inventoryService.getAllLatest();
    }

    @GetMapping("/{machineId}")
    public InventoryStatus getForMachine(@PathVariable String machineId) {
        return inventoryService.getForMachine(machineId);
    }

    @GetMapping("/{machineId}/history")
    public List<InventoryStatus> history(@PathVariable String machineId) {
        return inventoryService.getHistory(machineId);
    }
}
