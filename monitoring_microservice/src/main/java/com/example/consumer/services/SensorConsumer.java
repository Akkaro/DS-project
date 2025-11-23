package com.example.consumer.services;

import com.example.consumer.entities.Device;
import com.example.consumer.entities.HourlyConsumption;
import com.example.consumer.repositories.DeviceRepository;
import com.example.consumer.repositories.HourlyConsumptionRepository;
import com.example.consumer.dtos.SensorDataDTO; // Create this DTO to match JSON fields
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SensorConsumer {

    private final HourlyConsumptionRepository consumptionRepository;
    private final DeviceRepository deviceRepository;

    // In-memory buffer: Map<DeviceId, List<MeasurementValues>>
    private final Map<UUID, List<Double>> buffer = new ConcurrentHashMap<>();

    public SensorConsumer(HourlyConsumptionRepository consumptionRepository, DeviceRepository deviceRepository) {
        this.consumptionRepository = consumptionRepository;
        this.deviceRepository = deviceRepository;
    }

    @RabbitListener(queues = "${app.queue.sensor}")
    public void receiveSensorData(SensorDataDTO data) {
        UUID deviceId = data.getDeviceId();

        // 1. Validate Device (Requirements: "first must be verified if a device exists")
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) {
            System.out.println("Received data for unknown device: " + deviceId);
            return;
        }

        // 2. Buffer Logic
        buffer.putIfAbsent(deviceId, new ArrayList<>());
        List<Double> measurements = buffer.get(deviceId);
        measurements.add(data.getMeasurementValue());

        System.out.println("Buffer for " + deviceId + ": " + measurements.size() + "/6");

        // 3. Process if full (Requirements: "when the sixth comes, we sum them up")
        if (measurements.size() >= 6) {
            double total = measurements.stream().mapToDouble(Double::doubleValue).sum();
            
            HourlyConsumption hourly = new HourlyConsumption();
            hourly.setDeviceId(deviceId);
            hourly.setTimestamp(LocalDateTime.now());
            hourly.setTotalConsumption(total);
            
            consumptionRepository.save(hourly);
            System.out.println("Saved hourly data for " + deviceId + ": " + total);
            
            measurements.clear();
        }
    }
}