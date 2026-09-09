package com.beanbrew.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_status")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "machine_id", nullable = false)
    private String machineId;

    @Column(name = "coffee_powder_level", nullable = false)
    private Double coffeePowderLevel;

    @Column(name = "milk_level", nullable = false)
    private Double milkLevel;

    @Column(name = "water_level", nullable = false)
    private Double waterLevel;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
