package com.beanbrew.dto;

import com.beanbrew.entity.MachineStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MachineDTO {
    private String machineId;
    private String location;
    private String department;
    private MachineStatus status;
    private Double temperature;
    private Double waterLevel;
    private Double milkLevel;
    private Double coffeePowderLevel;
    private LocalDateTime lastSeen;
    private Long totalDowntimeMinutes;
    private Integer failureCount;
}
