package com.example.consumer.services;

import com.example.consumer.entities.Device;
import com.example.consumer.repositories.DeviceRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class SyncConsumer {

    private final DeviceRepository deviceRepository;

    public SyncConsumer(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @RabbitListener(queues = "monitoring.sync.queue") 
    public void receiveSyncMessage(Map<String, Object> message) {
        try {
            String action = (String) message.get("action");
            Object deviceIdObj = message.get("deviceId");
            
            if (deviceIdObj == null) return;
            UUID deviceId = UUID.fromString(deviceIdObj.toString());

            if ("create_device".equals(action) || "update_device".equals(action)) {
                // For update, try to find existing, or create new if not found
                Device device = deviceRepository.findById(deviceId).orElse(new Device());
                device.setId(deviceId);
                
                Object maxConsObj = message.get("maxConsumption");
                if (maxConsObj instanceof Number) {
                    device.setMaxConsumption(((Number) maxConsObj).doubleValue());
                }

                Object userIdObj = message.get("userId");
                if (userIdObj != null) {
                    device.setUserId(UUID.fromString(userIdObj.toString()));
                } else {
                    // Explicitly set to null if missing (unassigned)
                    device.setUserId(null);
                }

                deviceRepository.save(device);
                System.out.println("Synced device update: " + deviceId);

            } else if ("delete_device".equals(action)) {
                deviceRepository.deleteById(deviceId);
                System.out.println("Synced deleted device: " + deviceId);
            }
            
        } catch (Exception e) {
            System.err.println("Error processing sync message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}