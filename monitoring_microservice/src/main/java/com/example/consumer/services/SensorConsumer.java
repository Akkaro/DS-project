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

    private final Map<UUID, List<Double>> buffer = new ConcurrentHashMap<>();
    
    private final Map<UUID, Long> currentBatchTimestamp = new ConcurrentHashMap<>();

    public SensorConsumer(HourlyConsumptionRepository consumptionRepository, DeviceRepository deviceRepository) {
        this.consumptionRepository = consumptionRepository;
        this.deviceRepository = deviceRepository;
    }

    @RabbitListener(queues = "${app.queue.sensor}")
    public void receiveSensorData(SensorDataDTO data) {
        UUID deviceId = data.getDeviceId();

        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) {
            System.out.println("Discarded data for unknown/unregistered device: " + deviceId);
            return;
        }

        buffer.putIfAbsent(deviceId, new ArrayList<>());
        List<Double> measurements = buffer.get(deviceId);

        if (measurements.isEmpty()) {
            currentBatchTimestamp.put(deviceId, data.getTimestamp());
        }

        measurements.add(data.getMeasurementValue());
        System.out.println("Buffer for " + deviceId + ": " + measurements.size() + "/6");

        if (measurements.size() >= 6) {
            double total = measurements.stream().mapToDouble(Double::doubleValue).sum();
            
            long batchTime = currentBatchTimestamp.get(deviceId);
            LocalDateTime date = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(batchTime), 
                    ZoneId.of("UTC")
            );

            HourlyConsumption hourly = new HourlyConsumption();
            hourly.setDeviceId(deviceId);
            hourly.setTimestamp(date);
            hourly.setTotalConsumption(total);
            
            consumptionRepository.save(hourly);
            System.out.println("Saved hourly data for " + deviceId + " at " + date + ": " + total);
            
            measurements.clear();
            currentBatchTimestamp.remove(deviceId);
        }
    }
}