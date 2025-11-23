package com.example.consumer.controllers;

import com.example.consumer.entities.HourlyConsumption;
import com.example.consumer.repositories.HourlyConsumptionRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private final HourlyConsumptionRepository repository;

    public MonitoringController(HourlyConsumptionRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/consumption/{deviceId}")
    public ResponseEntity<List<HourlyConsumption>> getDailyConsumption(
            @PathVariable UUID deviceId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<HourlyConsumption> data = repository.findByDeviceIdAndTimestampBetween(deviceId, startOfDay, endOfDay);
        return ResponseEntity.ok(data);
    }
}