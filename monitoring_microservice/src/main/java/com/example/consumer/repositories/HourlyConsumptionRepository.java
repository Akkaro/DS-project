package com.example.consumer.repositories;

import com.example.consumer.entities.HourlyConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface HourlyConsumptionRepository extends JpaRepository<HourlyConsumption, Long> {
    List<HourlyConsumption> findByDeviceIdAndTimestampBetween(UUID deviceId, LocalDateTime start, LocalDateTime end);
}