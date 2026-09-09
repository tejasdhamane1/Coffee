package com.beanbrew.repository;

import com.beanbrew.entity.AlertStatus;
import com.beanbrew.entity.MaintenanceAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MaintenanceAlertRepository extends JpaRepository<MaintenanceAlert, Long> {
    List<MaintenanceAlert> findByStatusOrderByCreatedAtDesc(AlertStatus status);
    List<MaintenanceAlert> findByMachineIdOrderByCreatedAtDesc(String machineId);
    List<MaintenanceAlert> findAllByOrderByCreatedAtDesc();
    long countByStatus(AlertStatus status);
}
