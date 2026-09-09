package com.beanbrew.service;

import com.beanbrew.entity.*;
import com.beanbrew.exception.ResourceNotFoundException;
import com.beanbrew.repository.MaintenanceAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final MaintenanceAlertRepository alertRepository;

    public List<MaintenanceAlert> getAll() {
        return alertRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<MaintenanceAlert> getActive() {
        return alertRepository.findByStatusOrderByCreatedAtDesc(AlertStatus.ACTIVE);
    }

    public List<MaintenanceAlert> getForMachine(String machineId) {
        return alertRepository.findByMachineIdOrderByCreatedAtDesc(machineId);
    }

    public MaintenanceAlert raise(String machineId, AlertType type, String message, AlertSeverity severity) {
        // avoid duplicate active alerts of the same type for the same machine
        boolean exists = alertRepository.findByMachineIdOrderByCreatedAtDesc(machineId).stream()
                .anyMatch(a -> a.getAlertType() == type && a.getStatus() == AlertStatus.ACTIVE);
        if (exists) return null;

        MaintenanceAlert alert = MaintenanceAlert.builder()
                .machineId(machineId)
                .alertType(type)
                .message(message)
                .severity(severity)
                .status(AlertStatus.ACTIVE)
                .build();
        return alertRepository.save(alert);
    }

    public MaintenanceAlert updateStatus(Long id, AlertStatus status) {
        MaintenanceAlert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found: " + id));
        alert.setStatus(status);
        if (status == AlertStatus.RESOLVED) {
            alert.setResolvedAt(LocalDateTime.now());
        }
        return alertRepository.save(alert);
    }

    public long countActive() {
        return alertRepository.countByStatus(AlertStatus.ACTIVE);
    }
}
