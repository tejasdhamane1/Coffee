package com.beanbrew.config;

import com.beanbrew.entity.*;
import com.beanbrew.repository.*;
import com.beanbrew.service.PredictionService;
import com.beanbrew.simulator.SimulatorEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * Seeds demo users, 6 machines across 4 offices, and 10 days of realistic historical
 * consumption so charts, predictions and analytics are meaningful immediately after setup.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VendingMachineRepository machineRepository;
    private final ConsumptionLogRepository consumptionLogRepository;
    private final InventoryStatusRepository inventoryStatusRepository;
    private final MaintenanceAlertRepository alertRepository;
    private final PasswordEncoder passwordEncoder;
    private final PredictionService predictionService;

    private final Random random = new Random(42);

    @Override
    public void run(String... args) {
        seedUsers();
        seedMachines();
        seedHistoricalConsumption();
        seedSampleAlert();
        predictionService.recomputeAll();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) return;
        userRepository.save(User.builder().name("Admin User").email("admin@beansandbrews.com")
                .password(passwordEncoder.encode("Admin@123")).role(Role.ADMIN).build());
        userRepository.save(User.builder().name("Office Manager").email("manager@beansandbrews.com")
                .password(passwordEncoder.encode("Manager@123")).role(Role.MANAGER).build());
        userRepository.save(User.builder().name("Field Operator").email("operator@beansandbrews.com")
                .password(passwordEncoder.encode("Operator@123")).role(Role.OPERATOR).build());
    }

    private void seedMachines() {
        if (machineRepository.count() > 0) return;

        record Seed(String id, String location, String department, double water, double milk, double coffee) {}
        List<Seed> seeds = List.of(
                new Seed("CM001", "Pune Office", "Engineering", 82, 65, 68),
                new Seed("CM002", "Pune Office", "HR", 90, 88, 91),
                new Seed("CM003", "Mumbai Office", "Finance", 45, 38, 42),
                new Seed("CM004", "Bangalore Office", "Engineering", 76, 70, 74),
                new Seed("CM005", "Hyderabad Office", "Management", 95, 92, 89),
                new Seed("CM006", "Pune Office", "Cafeteria", 60, 30, 25)
        );

        for (Seed s : seeds) {
            MachineStatus status = MachineStatus.ONLINE;
            if (s.id().equals("CM003")) status = MachineStatus.WARNING;

            machineRepository.save(VendingMachine.builder()
                    .machineId(s.id())
                    .location(s.location())
                    .department(s.department())
                    .status(status)
                    .temperature(90 + random.nextDouble() * 6)
                    .waterLevel(s.water())
                    .milkLevel(s.milk())
                    .coffeePowderLevel(s.coffee())
                    .lastSeen(LocalDateTime.now())
                    .totalDowntimeMinutes((long) (random.nextInt(120)))
                    .failureCount(random.nextInt(3))
                    .build());

            inventoryStatusRepository.save(InventoryStatus.builder()
                    .machineId(s.id())
                    .coffeePowderLevel(s.coffee())
                    .milkLevel(s.milk())
                    .waterLevel(s.water())
                    .build());
        }
    }

    private void seedHistoricalConsumption() {
        if (consumptionLogRepository.count() > 0) return;

        List<VendingMachine> machines = machineRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (int daysAgo = 10; daysAgo >= 1; daysAgo--) {
            LocalDateTime day = now.minusDays(daysAgo).withHour(0).withMinute(0);
            for (VendingMachine machine : machines) {
                for (int hour = 7; hour <= 21; hour++) {
                    int cups = SimulatorEngine.cupsForTick(hour, machine.getDepartment());
                    for (int c = 0; c < cups; c++) {
                        CoffeeType type = SimulatorEngine.weightedCoffeeType();
                        int minute = random.nextInt(60);
                        consumptionLogRepository.save(ConsumptionLog.builder()
                                .machineId(machine.getMachineId())
                                .coffeeType(type)
                                .cupsDispensed(1)
                                .timestamp(day.withHour(hour).withMinute(minute))
                                .isSeedData(true)
                                .build());
                    }
                }
            }
        }

        // A light dusting of "today" activity so the dashboard isn't empty on first load.
        int currentHour = now.getHour();
        for (VendingMachine machine : machines) {
            for (int hour = 7; hour <= currentHour; hour++) {
                int cups = SimulatorEngine.cupsForTick(hour, machine.getDepartment());
                for (int c = 0; c < cups; c++) {
                    consumptionLogRepository.save(ConsumptionLog.builder()
                            .machineId(machine.getMachineId())
                            .coffeeType(SimulatorEngine.weightedCoffeeType())
                            .cupsDispensed(1)
                            .timestamp(now.withHour(hour).withMinute(random.nextInt(60)))
                            .isSeedData(true)
                            .build());
                }
            }
        }
    }

    private void seedSampleAlert() {
        if (alertRepository.count() > 0) return;
        alertRepository.save(MaintenanceAlert.builder()
                .machineId("CM003")
                .alertType(AlertType.LOW_MILK)
                .message("Milk level below warning threshold (38%).")
                .severity(AlertSeverity.MEDIUM)
                .status(AlertStatus.ACTIVE)
                .build());
    }
}
