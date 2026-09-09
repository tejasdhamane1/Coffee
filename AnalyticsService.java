package com.beanbrew.analytics;

import com.beanbrew.entity.*;
import com.beanbrew.repository.ConsumptionLogRepository;
import com.beanbrew.repository.MaintenanceAlertRepository;
import com.beanbrew.repository.RefillPredictionRepository;
import com.beanbrew.repository.VendingMachineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final VendingMachineRepository machineRepository;
    private final ConsumptionLogRepository consumptionLogRepository;
    private final MaintenanceAlertRepository alertRepository;
    private final RefillPredictionRepository predictionRepository;

    public Map<String, Object> overview() {
        List<VendingMachine> machines = machineRepository.findAll();
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        List<ConsumptionLog> todayLogs = consumptionLogRepository.findByTimestampBetween(startOfDay, endOfDay);

        long online = machines.stream().filter(m -> m.getStatus() == MachineStatus.ONLINE).count();
        long offline = machines.stream().filter(m -> m.getStatus() == MachineStatus.OFFLINE).count();
        long maintenance = machines.stream().filter(m -> m.getStatus() == MachineStatus.MAINTENANCE).count();
        long warning = machines.stream().filter(m -> m.getStatus() == MachineStatus.WARNING).count();

        int cupsToday = todayLogs.stream().mapToInt(ConsumptionLog::getCupsDispensed).sum();

        double avgDaily = averageDailyOverall();

        Map<CoffeeType, Long> countsByType = todayLogs.stream()
                .collect(Collectors.groupingBy(ConsumptionLog::getCoffeeType, Collectors.summingLong(ConsumptionLog::getCupsDispensed)));
        String mostPopular = countsByType.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> e.getKey().name())
                .orElse("N/A");

        long needsRefill = predictionRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(p -> p.getEstimatedDaysRemaining() != null && p.getEstimatedDaysRemaining() <= 3)
                .map(p -> p.getMachineId())
                .distinct()
                .count();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalMachines", machines.size());
        result.put("onlineMachines", online);
        result.put("offlineMachines", offline);
        result.put("maintenanceMachines", maintenance);
        result.put("warningMachines", warning);
        result.put("cupsDispensedToday", cupsToday);
        result.put("averageDailyConsumption", Math.round(avgDaily * 10.0) / 10.0);
        result.put("mostPopularCoffee", mostPopular);
        result.put("machinesRequiringRefill", needsRefill);
        result.put("activeAlerts", alertRepository.countByStatus(AlertStatus.ACTIVE));
        return result;
    }

    private double averageDailyOverall() {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        List<ConsumptionLog> logs = consumptionLogRepository.findRecentSince(start);
        int total = logs.stream().mapToInt(ConsumptionLog::getCupsDispensed).sum();
        return total / 7.0;
    }

    public Map<String, Object> coffeePreferences() {
        List<ConsumptionLog> logs = consumptionLogRepository.findAll();
        Map<CoffeeType, Long> counts = new EnumMap<>(CoffeeType.class);
        for (CoffeeType t : CoffeeType.values()) counts.put(t, 0L);
        for (ConsumptionLog log : logs) {
            counts.merge(log.getCoffeeType(), (long) log.getCupsDispensed(), Long::sum);
        }
        long total = counts.values().stream().mapToLong(Long::longValue).sum();

        List<Map<String, Object>> distribution = new ArrayList<>();
        for (Map.Entry<CoffeeType, Long> e : counts.entrySet()) {
            double pct = total == 0 ? 0 : (e.getValue() * 100.0 / total);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("coffeeType", e.getKey().name());
            row.put("cups", e.getValue());
            row.put("percentage", Math.round(pct * 10.0) / 10.0);
            distribution.add(row);
        }
        distribution.sort((a, b) -> Long.compare((Long) b.get("cups"), (Long) a.get("cups")));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("distribution", distribution);
        result.put("mostPopular", distribution.isEmpty() ? null : distribution.get(0).get("coffeeType"));
        result.put("leastPopular", distribution.isEmpty() ? null : distribution.get(distribution.size() - 1).get("coffeeType"));

        // department-wise preference
        Map<String, VendingMachine> machineById = machineRepository.findAll().stream()
                .collect(Collectors.toMap(VendingMachine::getMachineId, m -> m));
        Map<String, Map<CoffeeType, Long>> byDept = new LinkedHashMap<>();
        for (ConsumptionLog log : logs) {
            VendingMachine m = machineById.get(log.getMachineId());
            if (m == null) continue;
            byDept.computeIfAbsent(m.getDepartment(), k -> new EnumMap<>(CoffeeType.class))
                  .merge(log.getCoffeeType(), (long) log.getCupsDispensed(), Long::sum);
        }
        result.put("departmentPreference", byDept);
        return result;
    }

    public Map<String, Object> peakHours() {
        List<ConsumptionLog> logs = consumptionLogRepository.findAll();
        int[] hourly = new int[24];
        for (ConsumptionLog log : logs) {
            int hour = log.getTimestamp().getHour();
            hourly[hour] += log.getCupsDispensed();
        }
        List<Map<String, Object>> chart = new ArrayList<>();
        int peakHour = 0, peakCups = -1;
        for (int h = 0; h < 24; h++) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("hour", h);
            row.put("cups", hourly[h]);
            chart.add(row);
            if (hourly[h] > peakCups) {
                peakCups = hourly[h];
                peakHour = h;
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("hourly", chart);
        result.put("peakHour", peakHour);
        result.put("peakCups", peakCups);
        result.put("peakPeriod", String.format("%02d:00-%02d:00", peakHour, (peakHour + 1) % 24));
        return result;
    }

    public Map<String, Object> departmentAnalytics() {
        Map<String, VendingMachine> machineById = machineRepository.findAll().stream()
                .collect(Collectors.toMap(VendingMachine::getMachineId, m -> m));
        Map<String, Long> byDept = new LinkedHashMap<>();
        for (ConsumptionLog log : consumptionLogRepository.findAll()) {
            VendingMachine m = machineById.get(log.getMachineId());
            if (m == null) continue;
            byDept.merge(m.getDepartment(), (long) log.getCupsDispensed(), Long::sum);
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        byDept.forEach((dept, cups) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("department", dept);
            row.put("cups", cups);
            rows.add(row);
        });
        rows.sort((a, b) -> Long.compare((Long) b.get("cups"), (Long) a.get("cups")));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("departments", rows);
        return result;
    }

    public Map<String, Object> consumptionTrend(String range) {
        List<ConsumptionLog> logs = consumptionLogRepository.findAll();
        Map<String, Integer> grouped = new TreeMap<>();

        for (ConsumptionLog log : logs) {
            String key = switch (range) {
                case "weekly" -> log.getTimestamp().toLocalDate().minusDays(log.getTimestamp().getDayOfWeek().getValue() - 1).toString();
                case "monthly" -> log.getTimestamp().getYear() + "-" + String.format("%02d", log.getTimestamp().getMonthValue());
                default -> log.getTimestamp().toLocalDate().toString();
            };
            grouped.merge(key, log.getCupsDispensed(), Integer::sum);
        }

        List<Map<String, Object>> series = new ArrayList<>();
        grouped.forEach((k, v) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("period", k);
            row.put("cups", v);
            series.add(row);
        });
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("range", range);
        result.put("series", series);
        return result;
    }
}
