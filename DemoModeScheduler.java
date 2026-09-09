package com.beanbrew.config;

import com.beanbrew.service.PredictionService;
import com.beanbrew.service.SimulatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DemoModeScheduler {

    private final SimulatorService simulatorService;
    private final PredictionService predictionService;

    // Advances demo-mode office activity every 15 seconds so a live demo shows visible movement.
    @Scheduled(fixedRate = 15000)
    public void tick() {
        simulatorService.tick();
    }

    // Recomputes refill predictions every 5 minutes for all machines.
    @Scheduled(fixedRate = 300000)
    public void refreshPredictions() {
        predictionService.recomputeAll();
    }
}
