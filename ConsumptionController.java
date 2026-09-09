package com.beanbrew.controller;

import com.beanbrew.entity.ConsumptionLog;
import com.beanbrew.service.ConsumptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consumption")
@RequiredArgsConstructor
public class ConsumptionController {

    private final ConsumptionService consumptionService;

    @GetMapping
    public List<ConsumptionLog> getAll() {
        return consumptionService.getAll();
    }

    @GetMapping("/today")
    public Map<String, Object> today() {
        return Map.of(
                "cups", consumptionService.getTodayTotal(),
                "logs", consumptionService.getToday()
        );
    }

    @GetMapping("/machine/{machineId}")
    public List<ConsumptionLog> byMachine(@PathVariable String machineId) {
        return consumptionService.getByMachine(machineId);
    }
}
