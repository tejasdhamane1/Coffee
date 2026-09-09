package com.beanbrew.controller;

import com.beanbrew.dto.DispenseRequest;
import com.beanbrew.service.SimulatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/simulator")
@RequiredArgsConstructor
public class SimulatorController {

    private final SimulatorService simulatorService;

    @PostMapping("/dispense")
    public Map<String, Object> dispense(@Valid @RequestBody DispenseRequest request) {
        return simulatorService.dispense(request.getMachineId(), request.getCoffeeType());
    }

    @PostMapping("/fault/{machineId}/{faultType}")
    public Map<String, String> triggerFault(@PathVariable String machineId, @PathVariable String faultType) {
        simulatorService.simulateFault(machineId, faultType);
        return Map.of("machineId", machineId, "fault", faultType, "status", "applied");
    }

    @PostMapping("/start")
    public Map<String, Object> start() {
        simulatorService.startDemoMode();
        return Map.of("demoMode", true);
    }

    @PostMapping("/stop")
    public Map<String, Object> stop() {
        simulatorService.stopDemoMode();
        return Map.of("demoMode", false);
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of("demoMode", simulatorService.isDemoModeRunning());
    }
}
