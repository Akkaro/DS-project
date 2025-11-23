package com.example.consumer.services;

import com.example.consumer.entities.Device;
import com.example.consumer.repositories.DeviceRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class SyncConsumer {

    private final DeviceRepository deviceRepository;

    public SyncConsumer(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @RabbitListener(queues = "${app.queue.sync}")
    public void receiveSyncMessage(Map<String, Object> message) {
        try {
            String action = (String) message.get("action");
            String deviceIdStr = (String) message.get("deviceId").toString();
            UUID deviceId = UUID.fromString(deviceIdStr);

            if ("create_device".equals(action)) {
                Device device = new Device();
                device.setId(deviceId);
                
                // Handle Double conversion safely
                Object maxConsObj = message.get("maxConsumption");
                if (maxConsObj instanceof Number) {
                    device.setMaxConsumption(((Number) maxConsObj).doubleValue());
                }

                Object userIdObj = message.get("userId");
                if (userIdObj != null) {
                    device.setUserId(UUID.fromString(userIdObj.toString()));
                }

                deviceRepository.save(device);
                System.out.println("Synced new device: " + deviceId);

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