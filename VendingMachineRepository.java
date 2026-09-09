package com.beanbrew.repository;

import com.beanbrew.entity.MachineStatus;
import com.beanbrew.entity.VendingMachine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VendingMachineRepository extends JpaRepository<VendingMachine, String> {
    List<VendingMachine> findByStatus(MachineStatus status);
    List<VendingMachine> findByDepartment(String department);
    List<VendingMachine> findByLocation(String location);
}
