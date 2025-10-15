package com.example.demo.controllers;

import com.example.demo.dtos.DeviceDTO;
import com.example.demo.dtos.DeviceDetailsDTO;
import com.example.demo.entities.DeviceStatus;
import com.example.demo.services.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/devices")
@Validated
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public ResponseEntity<List<DeviceDTO>> getDevices() {
        return ResponseEntity.ok(deviceService.findDevices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceDetailsDTO> getDevice(@PathVariable UUID id) {
        return ResponseEntity.ok(deviceService.findDeviceById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DeviceDTO>> getDevicesByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(deviceService.findDevicesByUserId(userId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<DeviceDTO>> getDevicesByStatus(@PathVariable DeviceStatus status) {
        return ResponseEntity.ok(deviceService.findDevicesByStatus(status));
    }

    @GetMapping("/unassigned")
    public ResponseEntity<List<DeviceDTO>> getUnassignedDevices() {
        return ResponseEntity.ok(deviceService.findUnassignedDevices());
    }

    @PostMapping
    public ResponseEntity<Void> createDevice(@Valid @RequestBody DeviceDetailsDTO deviceDTO) {
        UUID id = deviceService.insert(deviceDTO);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateDevice(@PathVariable UUID id, 
                                             @Valid @RequestBody DeviceDetailsDTO deviceDTO) {
        deviceService.update(id, deviceDTO);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{deviceId}/assign/{userId}")
    public ResponseEntity<Void> assignDeviceToUser(@PathVariable UUID deviceId, 
                                                   @PathVariable UUID userId) {
        deviceService.assignDeviceToUser(deviceId, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{deviceId}/unassign")
    public ResponseEntity<Void> unassignDevice(@PathVariable UUID deviceId) {
        deviceService.unassignDevice(deviceId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable UUID id) {
        deviceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Long> countDevicesByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(deviceService.countDevicesByUser(userId));
    }
}