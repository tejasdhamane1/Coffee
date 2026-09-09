package com.beanbrew.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vending_machines")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VendingMachine {

    @Id
    private String machineId;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MachineStatus status = MachineStatus.ONLINE;

    @Builder.Default
    private Double temperature = 92.0;

    @Column(name = "water_level")
    @Builder.Default
    private Double waterLevel = 100.0;

    @Column(name = "milk_level")
    @Builder.Default
    private Double milkLevel = 100.0;

    @Column(name = "coffee_powder_level")
    @Builder.Default
    private Double coffeePowderLevel = 100.0;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    @Column(name = "offline_since")
    private LocalDateTime offlineSince;

    @Column(name = "total_downtime_minutes")
    @Builder.Default
    private Long totalDowntimeMinutes = 0L;

    @Column(name = "failure_count")
    @Builder.Default
    private Integer failureCount = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (lastSeen == null) lastSeen = LocalDateTime.now();
    }
}
