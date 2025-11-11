package com.example.demo.controllers;

import com.example.demo.dtos.DeviceDTO;
import com.example.demo.dtos.DeviceDetailsDTO;
import com.example.demo.entities.DeviceStatus;
import com.example.demo.services.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
@Validated
@Tag(name = "Device Management", description = "Endpoints for managing devices and user assignments.")
@SecurityRequirement(name = "bearerAuth")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Operation(summary = "Get all devices (Admin Only)")
    @ApiResponse(responseCode = "200", description = "List of all devices")
    @ApiResponse(responseCode = "403", description = "Forbidden (User is not ADMIN)")
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<DeviceDTO>> getDevices() {
        return ResponseEntity.ok(deviceService.findDevices());
    }

    @Operation(summary = "Get device by ID (Admin Only)")
    @ApiResponse(responseCode = "200", description = "Device found")
    @ApiResponse(responseCode = "403", description = "Forbidden (User is not ADMIN)")
    @ApiResponse(responseCode = "404", description = "Device not found")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<DeviceDetailsDTO> getDevice(@PathVariable UUID id) {
        return ResponseEntity.ok(deviceService.findDeviceById(id));
    }

    @Operation(summary = "Get devices for a specific user (Admin or Client-Owner)",
               description = "Requires ADMIN role, OR a CLIENT role where the {userId} matches their own.")
    @ApiResponse(responseCode = "200", description = "List of devices for the user")
    @ApiResponse(responseCode = "403", description = "Forbidden (Client trying to access another user's devices)")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('CLIENT') and #userId.toString() == principal)")
    public ResponseEntity<List<DeviceDTO>> getDevicesByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(deviceService.findDevicesByUserId(userId));
    }

    @Operation(summary = "Get devices by status (Admin Only)")
    @ApiResponse(responseCode = "200", description = "List of devices matching the status")
    @ApiResponse(responseCode = "403", description = "Forbidden (User is not ADMIN)")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<DeviceDTO>> getDevicesByStatus(@PathVariable DeviceStatus status) {
        return ResponseEntity.ok(deviceService.findDevicesByStatus(status));
    }

    @Operation(summary = "Get unassigned devices (Admin Only)")
    @ApiResponse(responseCode = "200", description = "List of unassigned devices")
    @ApiResponse(responseCode = "403", description = "Forbidden (User is not ADMIN)")
    @GetMapping("/unassigned")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<DeviceDTO>> getUnassignedDevices() {
        return ResponseEntity.ok(deviceService.findUnassignedDevices());
    }

    @Operation(summary = "Create a new device (Admin Only)")
    @ApiResponse(responseCode = "201", description = "Device created")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "403", description = "Forbidden (User is not ADMIN)")
    @ApiResponse(responseCode = "409", description = "Device name already exists")
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> createDevice(@Valid @RequestBody DeviceDetailsDTO deviceDTO) {
        UUID id = deviceService.insert(deviceDTO);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @Operation(summary = "Update a device (Admin Only)")
    @ApiResponse(responseCode = "204", description = "Device updated successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "403", description = "Forbidden (User is not ADMIN)")
    @ApiResponse(responseCode = "404", description = "Device not found")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> updateDevice(@PathVariable UUID id, 
                                             @Valid @RequestBody DeviceDetailsDTO deviceDTO) {
        deviceService.update(id, deviceDTO);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Assign a device to a user (Admin Only)")
    @ApiResponse(responseCode = "204", description = "Device assigned successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden (User is not ADMIN)")
    @ApiResponse(responseCode = "404", description = "Device or User not found")
    @PutMapping("/{deviceId}/assign/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> assignDeviceToUser(@PathVariable UUID deviceId, 
                                                   @PathVariable UUID userId) {
        deviceService.assignDeviceToUser(deviceId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Unassign a device (Admin Only)")
    @ApiResponse(responseCode = "204", description = "Device unassigned successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden (User is not ADMIN)")
    @ApiResponse(responseCode = "404", description = "Device not found")
    @PutMapping("/{deviceId}/unassign")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> unassignDevice(@PathVariable UUID deviceId) {
        deviceService.unassignDevice(deviceId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete a device (Admin Only)")
    @ApiResponse(responseCode = "204", description = "Device deleted successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden (User is not ADMIN)")
    @ApiResponse(responseCode = "404", description = "Device not found")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteDevice(@PathVariable UUID id) {
        deviceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Count devices for a user (Admin Only)")
    @ApiResponse(responseCode = "200", description = "Count of devices")
    @ApiResponse(responseCode = "403", description = "Forbidden (User is not ADMIN)")
    @GetMapping("/user/{userId}/count")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Long> countDevicesByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(deviceService.countDevicesByUser(userId));
    }
}