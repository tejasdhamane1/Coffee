package com.beanbrew.service;

import com.beanbrew.entity.ConsumptionLog;
import com.beanbrew.entity.CoffeeType;
import com.beanbrew.repository.ConsumptionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsumptionService {

    private final ConsumptionLogRepository consumptionLogRepository;

    public ConsumptionLog log(String machineId, CoffeeType coffeeType, int cups, boolean seed) {
        ConsumptionLog log = ConsumptionLog.builder()
                .machineId(machineId)
                .coffeeType(coffeeType)
                .cupsDispensed(cups)
                .timestamp(LocalDateTime.now())
                .isSeedData(seed)
                .build();
        return consumptionLogRepository.save(log);
    }

    public ConsumptionLog logAt(String machineId, CoffeeType coffeeType, int cups, LocalDateTime timestamp, boolean seed) {
        ConsumptionLog log = ConsumptionLog.builder()
                .machineId(machineId)
                .coffeeType(coffeeType)
                .cupsDispensed(cups)
                .timestamp(timestamp)
                .isSeedData(seed)
                .build();
        return consumptionLogRepository.save(log);
    }

    public List<ConsumptionLog> getAll() {
        return consumptionLogRepository.findAll();
    }

    public List<ConsumptionLog> getToday() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        return consumptionLogRepository.findByTimestampBetween(start, end);
    }

    public Long getTodayTotal() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        return consumptionLogRepository.sumCupsBetween(start, end);
    }

    public List<ConsumptionLog> getByMachine(String machineId) {
        return consumptionLogRepository.findByMachineId(machineId);
    }

    public double averageDailyConsumption(String machineId, int lookbackDays) {
        LocalDateTime start = LocalDateTime.now().minusDays(lookbackDays);
        List<ConsumptionLog> logs = consumptionLogRepository.findByMachineIdAndTimestampBetween(machineId, start, LocalDateTime.now());
        int total = logs.stream().mapToInt(ConsumptionLog::getCupsDispensed).sum();
        return lookbackDays == 0 ? total : (double) total / lookbackDays;
    }
}
