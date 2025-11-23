package com.example.producer.services;

import com.example.producer.config.RabbitConfig;
import com.example.producer.dtos.SensorDataDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SensorReadingProducer {

    private final RabbitTemplate rabbitTemplate;

    // Read the DEVICE_ID from docker-compose environment variables
    @Value("${DEVICE_ID}")
    private String deviceIdEnv;

    public SensorReadingProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // Runs every 10 seconds (representing 10 minutes in simulation time)
    @Scheduled(fixedRate = 10000)
    public void sendData() {
        try {
            // If deviceIdEnv is not set yet (e.g., running locally without env), use a random one
            UUID deviceId = (deviceIdEnv != null && !deviceIdEnv.isEmpty()) 
                            ? UUID.fromString(deviceIdEnv) 
                            : UUID.randomUUID();

            // Generate a simulated value (replace this with CSV reading logic later if needed)
            double measurement = Math.random() * 20; 

            SensorDataDTO data = new SensorDataDTO();
            data.setTimestamp(System.currentTimeMillis());
            data.setDeviceId(deviceId);
            data.setMeasurementValue(measurement);

            rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_NAME, data);
            
            System.out.println("Sent: " + measurement + " for device " + deviceId);
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}