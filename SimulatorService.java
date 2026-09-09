package com.beanbrew.service;

import com.beanbrew.entity.*;
import com.beanbrew.exception.ResourceNotFoundException;
import com.beanbrew.repository.VendingMachineRepository;
import com.beanbrew.simulator.SimulatorEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class SimulatorService {

    private final VendingMachineRepository machineRepository;
    private final MachineService machineService;
    private final ConsumptionService consumptionService;
    private final InventoryService inventoryService;
    private final MaintenanceService maintenanceService;
    private final PredictionService predictionService;

    private final AtomicBoolean demoModeRunning = new AtomicBoolean(false);

    /** Simulates a single, user-triggered coffee dispense — used by the interactive simulator UI. */
    public Map<String, Object> dispense(String machineId, CoffeeType coffeeType) {
        VendingMachine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found: " + machineId));

        if (machine.getStatus() == MachineStatus.OFFLINE || machine.getStatus() == MachineStatus.MAINTENANCE) {
            throw new IllegalStateException("Machine " + machineId + " is not available to dispense (" + machine.getStatus() + ")");
        }

        double depletion = SimulatorEngine.inventoryDepletionPerCup(coffeeType);
        double newCoffee = Math.max(0, machine.getCoffeePowderLevel() - depletion * 2.2);
        double newMilk = coffeeType == CoffeeType.ESPRESSO || coffeeType == CoffeeType.BLACK_COFFEE || coffeeType == CoffeeType.AMERICANO
                ? machine.getMilkLevel()
                : Math.max(0, machine.getMilkLevel() - depletion * 2.5);
        double newWater = Math.max(0, machine.getWaterLevel() - depletion * 1.8);

        machineService.updateInventory(machineId, newWater, newMilk, newCoffee);
        consumptionService.log(machineId, coffeeType, 1, false);
        inventoryService.recordSnapshot(machineId, newCoffee, newMilk, newWater);

        checkAndRaiseInventoryAlerts(machineId, newWater, newMilk, newCoffee);
        predictionService.recompute(machineId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("machineId", machineId);
        result.put("coffeeType", coffeeType);
        result.put("waterLevel", newWater);
        result.put("milkLevel", newMilk);
        result.put("coffeePowderLevel", newCoffee);
        result.put("status", "DISPENSED");
        return result;
    }

    public void checkAndRaiseInventoryAlerts(String machineId, double water, double milk, double coffee) {
        if (water < 15) maintenanceService.raise(machineId, AlertType.LOW_WATER, "Water level critically low (" + Math.round(water) + "%).", water < 8 ? AlertSeverity.CRITICAL : AlertSeverity.HIGH);
        if (milk < 15) maintenanceService.raise(machineId, AlertType.LOW_MILK, "Milk level critically low (" + Math.round(milk) + "%).", milk < 8 ? AlertSeverity.CRITICAL : AlertSeverity.HIGH);
        if (coffee < 15) maintenanceService.raise(machineId, AlertType.LOW_COFFEE_POWDER, "Coffee powder critically low (" + Math.round(coffee) + "%).", coffee < 8 ? AlertSeverity.CRITICAL : AlertSeverity.HIGH);

        VendingMachine machine = machineRepository.findById(machineId).orElse(null);
        if (machine != null) {
            MachineStatus current = machine.getStatus();
            boolean anyLow = water < 15 || milk < 15 || coffee < 15;
            if (anyLow && current == MachineStatus.ONLINE) {
                machineService.setStatus(machineId, MachineStatus.WARNING);
            }
        }
    }

    public void simulateFault(String machineId, String faultType) {
        switch (faultType) {
            case "LOW_WATER" -> { machineService.updateInventory(machineId, 8.0, null, null); checkAndRaiseInventoryAlerts(machineId, 8.0, 100.0, 100.0); }
            case "LOW_MILK" -> { machineService.updateInventory(machineId, null, 8.0, null); checkAndRaiseInventoryAlerts(machineId, 100.0, 8.0, 100.0); }
            case "LOW_COFFEE_POWDER" -> { machineService.updateInventory(machineId, null, null, 8.0); checkAndRaiseInventoryAlerts(machineId, 100.0, 100.0, 8.0); }
            case "HIGH_TEMPERATURE" -> {
                machineService.updateTemperature(machineId, 105.0);
                maintenanceService.raise(machineId, AlertType.HIGH_TEMPERATURE, "Brewing temperature abnormally high (105 C).", AlertSeverity.HIGH);
                machineService.setStatus(machineId, MachineStatus.WARNING);
            }
            case "HEATER_FAILURE" -> {
                machineService.updateTemperature(machineId, 20.0);
                maintenanceService.raise(machineId, AlertType.HEATER_FAILURE, "Heater failure detected — unable to reach brewing temperature.", AlertSeverity.CRITICAL);
                machineService.setStatus(machineId, MachineStatus.MAINTENANCE);
            }
            case "MACHINE_FAILURE" -> {
                maintenanceService.raise(machineId, AlertType.ABNORMAL_BEHAVIOUR, "Machine reported abnormal internal behaviour.", AlertSeverity.CRITICAL);
                machineService.setStatus(machineId, MachineStatus.MAINTENANCE);
            }
            case "NETWORK_DISCONNECTION" -> {
                maintenanceService.raise(machineId, AlertType.NETWORK_DISCONNECTION, "Machine lost network connectivity.", AlertSeverity.MEDIUM);
                machineService.setStatus(machineId, MachineStatus.OFFLINE);
            }
            case "MACHINE_OFFLINE" -> {
                maintenanceService.raise(machineId, AlertType.MACHINE_OFFLINE, "Machine went offline.", AlertSeverity.MEDIUM);
                machineService.setStatus(machineId, MachineStatus.OFFLINE);
            }
            case "RESTORE" -> machineService.setStatus(machineId, MachineStatus.ONLINE);
            default -> throw new IllegalArgumentException("Unknown fault type: " + faultType);
        }
        predictionService.recompute(machineId);
    }

    public boolean isDemoModeRunning() {
        return demoModeRunning.get();
    }

    public void startDemoMode() {
        demoModeRunning.set(true);
    }

    public void stopDemoMode() {
        demoModeRunning.set(false);
    }

    /** Advances the whole simulated office by one "tick" — called by the scheduler when demo mode is on. */
    public void tick() {
        if (!demoModeRunning.get()) return;
        int hour = SimulatorEngine.currentHour();
        for (VendingMachine machine : machineRepository.findAll()) {
            if (machine.getStatus() == MachineStatus.OFFLINE || machine.getStatus() == MachineStatus.MAINTENANCE) continue;
            int cups = SimulatorEngine.cupsForTick(hour, machine.getDepartment());
            for (int i = 0; i < cups; i++) {
                try {
                    dispense(machine.getMachineId(), SimulatorEngine.weightedCoffeeType());
                } catch (Exception ignored) { /* machine went unavailable mid-loop */ }
            }
        }
    }
}
