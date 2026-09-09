package com.beanbrew.controller;

import com.beanbrew.dto.AlertResolveRequest;
import com.beanbrew.entity.AlertStatus;
import com.beanbrew.entity.MaintenanceAlert;
import com.beanbrew.service.MaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @GetMapping
    public List<MaintenanceAlert> getAll() {
        return maintenanceService.getAll();
    }

    @GetMapping("/active")
    public List<MaintenanceAlert> getActive() {
        return maintenanceService.getActive();
    }

    @PutMapping("/{id}/resolve")
    public MaintenanceAlert resolve(@PathVariable Long id) {
        return maintenanceService.updateStatus(id, AlertStatus.RESOLVED);
    }

    @PutMapping("/{id}/status")
    public MaintenanceAlert updateStatus(@PathVariable Long id, @RequestBody AlertResolveRequest request) {
        return maintenanceService.updateStatus(id, AlertStatus.valueOf(request.getStatus()));
    }
}
