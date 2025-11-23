package com.example.producer.services;

import com.example.producer.config.RabbitConfig;
import com.example.producer.dtos.SensorDataDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource; // Import for reading file
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.UUID;

@Service
public class SensorReadingProducer {

    private final RabbitTemplate rabbitTemplate;
    
    @Value("${DEVICE_ID}")
    private String deviceIdEnv;

    private BufferedReader reader;

    public SensorReadingProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        initializeReader();
    }

    // Helper to open/reset the CSV file
    private void initializeReader() {
        try {
            ClassPathResource resource = new ClassPathResource("sensor.csv");
            this.reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(fixedRate = 5000) // Runs every 5 seconds (representing 10 mins)
    public void sendData() {
        try {
            if (deviceIdEnv == null || deviceIdEnv.isEmpty()) {
                System.out.println("Waiting for DEVICE_ID configuration...");
                return;
            }
            UUID deviceId = UUID.fromString(deviceIdEnv);

            // Read next line
            String line = reader.readLine();
            
            // If end of file, reset reader and read again
            if (line == null) {
                initializeReader();
                line = reader.readLine();
            }

            if (line != null) {
                double measurement = Double.parseDouble(line.trim());

                SensorDataDTO data = new SensorDataDTO();
                data.setTimestamp(System.currentTimeMillis());
                data.setDeviceId(deviceId);
                data.setMeasurementValue(measurement);

                rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_NAME, data);
                System.out.println("Sent from CSV: " + measurement + " for device " + deviceId);
            }

        } catch (Exception e) {
            System.err.println("Error reading sensor data: " + e.getMessage());
        }
    }
}