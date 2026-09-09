package com.beanbrew.service;

import com.beanbrew.entity.InventoryType;
import com.beanbrew.entity.RefillPrediction;
import com.beanbrew.entity.VendingMachine;
import com.beanbrew.repository.RefillPredictionRepository;
import com.beanbrew.repository.VendingMachineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PredictionService {

    // Approximate daily depletion rate (% of tank per day) driven by cups/day at a typical machine.
    // Used as a fallback when there isn't enough historical consumption data yet.
    private static final double DEFAULT_DAILY_DEPLETION_PERCENT = 12.0;

    private final RefillPredictionRepository predictionRepository;
    private final VendingMachineRepository machineRepository;
    private final ConsumptionService consumptionService;

    public List<RefillPrediction> getAll() {
        return predictionRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<RefillPrediction> getForMachine(String machineId) {
        return predictionRepository.findByMachineIdOrderByCreatedAtDesc(machineId);
    }

    /** Recomputes and persists fresh predictions for a single machine, for all three inventory types. */
    public List<RefillPrediction> recompute(String machineId) {
        VendingMachine machine = machineRepository.findById(machineId).orElse(null);
        if (machine == null) return List.of();

        double avgDailyCups = consumptionService.averageDailyConsumption(machineId, 7);
        double dailyDepletionPercent = avgDailyCups > 0
                ? Math.min(30.0, avgDailyCups * 1.5)
                : DEFAULT_DAILY_DEPLETION_PERCENT;

        List<RefillPrediction> results = new ArrayList<>();
        results.add(buildAndSave(machineId, InventoryType.COFFEE_POWDER, machine.getCoffeePowderLevel(), dailyDepletionPercent));
        results.add(buildAndSave(machineId, InventoryType.MILK, machine.getMilkLevel(), dailyDepletionPercent * 1.1));
        results.add(buildAndSave(machineId, InventoryType.WATER, machine.getWaterLevel(), dailyDepletionPercent * 0.8));
        return results;
    }

    private RefillPrediction buildAndSave(String machineId, InventoryType type, double remainingPercent, double avgDailyConsumption) {
        double estimatedDays;
        String recommendation;

        if (avgDailyConsumption <= 0.01) {
            estimatedDays = 30.0;
            recommendation = "Insufficient consumption history — using conservative estimate.";
        } else {
            estimatedDays = remainingPercent / avgDailyConsumption;
            if (estimatedDays <= 1) {
                recommendation = "Refill urgently — inventory will run out within a day.";
            } else if (estimatedDays <= 3) {
                recommendation = "Refill required within " + Math.round(estimatedDays) + " days.";
            } else if (estimatedDays <= 7) {
                recommendation = "Schedule refill within the week.";
            } else {
                recommendation = "Inventory healthy — no immediate action needed.";
            }
        }

        RefillPrediction prediction = RefillPrediction.builder()
                .machineId(machineId)
                .inventoryType(type)
                .remainingInventory(remainingPercent)
                .averageDailyConsumption(avgDailyConsumption)
                .estimatedDaysRemaining(Math.round(estimatedDays * 10.0) / 10.0)
                .recommendedAction(recommendation)
                .build();
        return predictionRepository.save(prediction);
    }

    public void recomputeAll() {
        machineRepository.findAll().forEach(m -> recompute(m.getMachineId()));
    }
}
