package com.example.consumer.services;

import com.example.consumer.dtos.SensorDataDTO;
import com.example.consumer.entities.Device;
import com.example.consumer.entities.HourlyConsumption;
import com.example.consumer.repositories.DeviceRepository;
import com.example.consumer.repositories.HourlyConsumptionRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SensorConsumer {

    private final HourlyConsumptionRepository consumptionRepository;
    private final DeviceRepository deviceRepository;

    // Buffer stores measurements. 
    // Note: In a real complex scenario, you might need to store (Value, Timestamp) pairs 
    // to ensure they belong to the same hour. 
    // For this assignment, simply buffering the values is usually acceptable 
    // assuming the CSV is ordered.
    private final Map<UUID, List<Double>> buffer = new ConcurrentHashMap<>();
    
    // We also need to track the timestamp of the *first* measurement in the batch
    // to label the hourly record correctly.
    private final Map<UUID, Long> currentBatchTimestamp = new ConcurrentHashMap<>();

    public SensorConsumer(HourlyConsumptionRepository consumptionRepository, DeviceRepository deviceRepository) {
        this.consumptionRepository = consumptionRepository;
        this.deviceRepository = deviceRepository;
    }

    @RabbitListener(queues = "${app.queue.sensor}")
    public void receiveSensorData(SensorDataDTO data) {
        UUID deviceId = data.getDeviceId();

        // 1. Validate Device (Your requirement: "If it is not a valid device... discard it")
        // This checks the LOCAL sync table.
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) {
            System.out.println("Discarded data for unknown/unregistered device: " + deviceId);
            return;
        }

        // 2. Initialize buffer if empty
        buffer.putIfAbsent(deviceId, new ArrayList<>());
        List<Double> measurements = buffer.get(deviceId);

        // 3. If this is the first measurement of the hour/batch, save its timestamp
        if (measurements.isEmpty()) {
            currentBatchTimestamp.put(deviceId, data.getTimestamp());
        }

        measurements.add(data.getMeasurementValue());
        System.out.println("Buffer for " + deviceId + ": " + measurements.size() + "/6");

        // 4. Process if buffer is full (6 measurements = 1 hour)
        if (measurements.size() >= 6) {
            double total = measurements.stream().mapToDouble(Double::doubleValue).sum();
            
            // Retrieve the timestamp of the START of this batch
            long batchTime = currentBatchTimestamp.get(deviceId);
            LocalDateTime date = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(batchTime), 
                    ZoneId.of("UTC") // Ensure consistent timezone
            );

            HourlyConsumption hourly = new HourlyConsumption();
            hourly.setDeviceId(deviceId);
            hourly.setTimestamp(date); // USE SIMULATION TIME
            hourly.setTotalConsumption(total);
            
            consumptionRepository.save(hourly);
            System.out.println("Saved hourly data for " + deviceId + " at " + date + ": " + total);
            
            measurements.clear();
            currentBatchTimestamp.remove(deviceId); // Clear timestamp for next batch
        }
    }
}