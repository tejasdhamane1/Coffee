package com.beanbrew.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "consumption_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsumptionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "machine_id", nullable = false)
    private String machineId;

    @Enumerated(EnumType.STRING)
    @Column(name = "coffee_type", nullable = false)
    private CoffeeType coffeeType;

    @Column(name = "cups_dispensed", nullable = false)
    @Builder.Default
    private Integer cupsDispensed = 1;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "is_seed_data")
    @Builder.Default
    private Boolean isSeedData = false;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) timestamp = LocalDateTime.now();
    }
}
