package com.example.producer.services;

import com.example.producer.config.RabbitConfig;
import com.example.producer.dtos.SensorDataDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.UUID;

@Service
public class SensorReadingProducer {

    private final RabbitTemplate rabbitTemplate;
    
    private String csvFileName = "sensor.csv";

    private BufferedReader reader;

    // BATCH SIZE: How many lines to process per scheduled tick.
    // 6 lines = 1 hour of data per tick.
    // Increase this to speed up simulation (e.g., 144 = 1 day per tick).
    private static final int BATCH_SIZE = 6; 

    public SensorReadingProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        initializeReader();
    }

    private void initializeReader() {
        try {
            ClassPathResource resource = new ClassPathResource(csvFileName);
            if (resource.exists()) {
                this.reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
                System.out.println("Opened CSV file: " + csvFileName);
            } else {
                System.err.println("CSV file not found: " + csvFileName);
            }
        } catch (Exception e) {
            System.err.println("Error initializing CSV reader: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Run every 1000ms (1 second)
    @Scheduled(fixedRate = 1000) 
    public void sendDataBatch() {
        if (reader == null) {
            return;
        }

        try {
            for (int i = 0; i < BATCH_SIZE; i++) {
                String line = reader.readLine();
                
                if (line == null) {
                    System.out.println("End of CSV reached. Restarting...");
                    try { reader.close(); } catch (Exception ignored) {} 
                    
                    reader = null;
                    break;
                }

                if (!line.trim().isEmpty()) {
                    processLine(line);
                }
            }
        } catch (Exception e) {
            System.err.println("Error sending batch: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processLine(String line) {
        try {
            String[] parts = line.split(",");
            
            if (parts.length < 3) {
                System.err.println("Invalid line format: " + line);
                return;
            }

            long timestamp = Long.parseLong(parts[0].trim());
            UUID deviceId = UUID.fromString(parts[1].trim());
            double measurement = Double.parseDouble(parts[2].trim());

            SensorDataDTO data = new SensorDataDTO(timestamp, deviceId, measurement);
            
            rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_NAME, data);

        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.err.println("Skipping malformed line: " + line + " Error: " + e.getMessage());
        }
    }
}