package com.example.consumer.services;

import com.example.consumer.dtos.NotificationDTO;
import com.example.consumer.dtos.SensorDataDTO;
import com.example.consumer.entities.Device;
import com.example.consumer.entities.HourlyConsumption;
import com.example.consumer.repositories.DeviceRepository;
import com.example.consumer.repositories.HourlyConsumptionRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    private final RabbitTemplate rabbitTemplate;

    private final Map<UUID, List<Double>> buffer = new ConcurrentHashMap<>();
    
    private final Map<UUID, Long> currentBatchTimestamp = new ConcurrentHashMap<>();

    public SensorConsumer(HourlyConsumptionRepository consumptionRepository, DeviceRepository deviceRepository, RabbitTemplate rabbitTemplate) {
        this.consumptionRepository = consumptionRepository;
        this.deviceRepository = deviceRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "#{queueConfig.getQueueName()}")
    public void receiveSensorData(SensorDataDTO data) {
        UUID deviceId = data.getDeviceId();

        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) {
            System.out.println("Discarded data for unknown/unregistered device: " + deviceId);
            return;
        }
        
        Device device = deviceOpt.get();

        buffer.putIfAbsent(deviceId, new ArrayList<>());
        List<Double> measurements = buffer.get(deviceId);

        if (measurements.isEmpty()) {
            currentBatchTimestamp.put(deviceId, data.getTimestamp());
        }

        measurements.add(data.getMeasurementValue());
        System.out.println("Buffer for " + deviceId + ": " + measurements.size() + "/6");

        if (measurements.size() >= 6) {
            double total = measurements.stream().mapToDouble(Double::doubleValue).sum();
            double totalConsumption = measurements.stream().mapToDouble(Double::doubleValue).sum();
            
            // --- NEW: Overconsumption Logic ---
            if (totalConsumption > device.getMaxConsumption()) {
                System.out.println("ALERT: Device " + deviceId + " exceeded max consumption!");
                
                String alertMsg = "Device " + device.getId() + " consumed " + totalConsumption + 
                                  "kW, exceeding limit of " + device.getMaxConsumption() + "kW.";
                
                // Send to Chat Service
                NotificationDTO notification = new NotificationDTO(device.getUserId(), alertMsg);
                rabbitTemplate.convertAndSend("notification.queue", notification);
            }
            // ----------------------------------

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