package com.beanbrew.service;

import com.beanbrew.dto.MachineCreateRequest;
import com.beanbrew.dto.MachineDTO;
import com.beanbrew.entity.MachineStatus;
import com.beanbrew.entity.VendingMachine;
import com.beanbrew.exception.ResourceNotFoundException;
import com.beanbrew.repository.VendingMachineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MachineService {

    private final VendingMachineRepository machineRepository;

    public List<MachineDTO> getAll() {
        return machineRepository.findAll().stream().map(this::toDto).toList();
    }

    public MachineDTO getById(String id) {
        return toDto(findEntity(id));
    }

    public VendingMachine findEntity(String id) {
        return machineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found: " + id));
    }

    public MachineDTO create(MachineCreateRequest request) {
        VendingMachine machine = VendingMachine.builder()
                .machineId(request.getMachineId())
                .location(request.getLocation())
                .department(request.getDepartment())
                .status(MachineStatus.ONLINE)
                .temperature(92.0)
                .waterLevel(100.0)
                .milkLevel(100.0)
                .coffeePowderLevel(100.0)
                .lastSeen(LocalDateTime.now())
                .build();
        return toDto(machineRepository.save(machine));
    }

    public MachineDTO update(String id, MachineCreateRequest request) {
        VendingMachine machine = findEntity(id);
        machine.setLocation(request.getLocation());
        machine.setDepartment(request.getDepartment());
        return toDto(machineRepository.save(machine));
    }

    public void delete(String id) {
        machineRepository.delete(findEntity(id));
    }

    public void setStatus(String id, MachineStatus status) {
        VendingMachine machine = findEntity(id);
        MachineStatus previous = machine.getStatus();
        machine.setStatus(status);
        machine.setLastSeen(LocalDateTime.now());

        if (status == MachineStatus.OFFLINE && previous != MachineStatus.OFFLINE) {
            machine.setOfflineSince(LocalDateTime.now());
            machine.setFailureCount(machine.getFailureCount() + 1);
        }
        if (status != MachineStatus.OFFLINE && previous == MachineStatus.OFFLINE && machine.getOfflineSince() != null) {
            long minutes = ChronoUnit.MINUTES.between(machine.getOfflineSince(), LocalDateTime.now());
            machine.setTotalDowntimeMinutes(machine.getTotalDowntimeMinutes() + Math.max(minutes, 1));
            machine.setOfflineSince(null);
        }
        machineRepository.save(machine);
    }

    public void updateInventory(String id, Double water, Double milk, Double coffeePowder) {
        VendingMachine machine = findEntity(id);
        if (water != null) machine.setWaterLevel(clamp(water));
        if (milk != null) machine.setMilkLevel(clamp(milk));
        if (coffeePowder != null) machine.setCoffeePowderLevel(clamp(coffeePowder));
        machine.setLastSeen(LocalDateTime.now());
        machineRepository.save(machine);
    }

    public void updateTemperature(String id, Double temperature) {
        VendingMachine machine = findEntity(id);
        machine.setTemperature(temperature);
        machine.setLastSeen(LocalDateTime.now());
        machineRepository.save(machine);
    }

    private Double clamp(Double value) {
        return Math.max(0.0, Math.min(100.0, value));
    }

    private MachineDTO toDto(VendingMachine m) {
        return MachineDTO.builder()
                .machineId(m.getMachineId())
                .location(m.getLocation())
                .department(m.getDepartment())
                .status(m.getStatus())
                .temperature(m.getTemperature())
                .waterLevel(m.getWaterLevel())
                .milkLevel(m.getMilkLevel())
                .coffeePowderLevel(m.getCoffeePowderLevel())
                .lastSeen(m.getLastSeen())
                .totalDowntimeMinutes(m.getTotalDowntimeMinutes())
                .failureCount(m.getFailureCount())
                .build();
    }
}
