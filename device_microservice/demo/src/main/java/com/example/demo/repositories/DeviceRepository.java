package com.example.demo.repositories;

import com.example.demo.entities.Device;
import com.example.demo.entities.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {

    /**
     * Find all devices assigned to a specific user
     */
    List<Device> findByUserId(UUID userId);

    /**
     * Find devices by status
     */
    List<Device> findByStatus(DeviceStatus status);

    /**
     * Find unassigned devices (no user assigned)
     */
    List<Device> findByUserIdIsNull();

    /**
     * Check if a device name exists
     */
    boolean existsByName(String name);

    /**
     * Count devices assigned to a user
     */
    long countByUserId(UUID userId);
}