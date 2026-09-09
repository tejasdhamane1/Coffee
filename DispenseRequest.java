package com.beanbrew.dto;

import com.beanbrew.entity.CoffeeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DispenseRequest {
    @NotBlank
    private String machineId;

    @NotNull
    private CoffeeType coffeeType;
}
