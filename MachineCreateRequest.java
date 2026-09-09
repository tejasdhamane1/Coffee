package com.beanbrew.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MachineCreateRequest {
    @NotBlank
    private String machineId;
    @NotBlank
    private String location;
    @NotBlank
    private String department;
}
