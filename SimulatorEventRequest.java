package com.beanbrew.dto;

import lombok.Data;

import java.util.Map;

@Data
public class SimulatorEventRequest {
    private String machineId;
    private String eventType;
    private Map<String, Object> payload;
}
