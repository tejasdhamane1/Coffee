package com.beanbrew.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "refill_predictions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RefillPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "machine_id", nullable = false)
    private String machineId;

    @Enumerated(EnumType.STRING)
    @Column(name = "inventory_type", nullable = false)
    private InventoryType inventoryType;

    @Column(name = "remaining_inventory", nullable = false)
    private Double remainingInventory;

    @Column(name = "average_daily_consumption", nullable = false)
    private Double averageDailyConsumption;

    @Column(name = "estimated_days_remaining")
    private Double estimatedDaysRemaining;

    @Column(name = "recommended_action", length = 300)
    private String recommendedAction;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
