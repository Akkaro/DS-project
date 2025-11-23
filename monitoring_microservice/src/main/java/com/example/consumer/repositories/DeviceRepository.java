package com.example.consumer.repositories;

import com.example.consumer.entities.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

// Change Long to UUID here
public interface DeviceRepository extends JpaRepository<Device, UUID> {
}