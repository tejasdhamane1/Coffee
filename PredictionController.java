package com.beanbrew.controller;

import com.beanbrew.entity.RefillPrediction;
import com.beanbrew.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;

    @GetMapping
    public List<RefillPrediction> getAll() {
        return predictionService.getAll();
    }

    @GetMapping("/{machineId}")
    public List<RefillPrediction> forMachine(@PathVariable String machineId) {
        return predictionService.getForMachine(machineId);
    }
}
