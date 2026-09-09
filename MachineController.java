package com.beanbrew.controller;

import com.beanbrew.dto.MachineCreateRequest;
import com.beanbrew.dto.MachineDTO;
import com.beanbrew.service.MachineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/machines")
@RequiredArgsConstructor
public class MachineController {

    private final MachineService machineService;

    @GetMapping
    public List<MachineDTO> getAll() {
        return machineService.getAll();
    }

    @GetMapping("/{id}")
    public MachineDTO getById(@PathVariable String id) {
        return machineService.getById(id);
    }

    @PostMapping
    public ResponseEntity<MachineDTO> create(@Valid @RequestBody MachineCreateRequest request) {
        return ResponseEntity.ok(machineService.create(request));
    }

    @PutMapping("/{id}")
    public MachineDTO update(@PathVariable String id, @Valid @RequestBody MachineCreateRequest request) {
        return machineService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        machineService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
