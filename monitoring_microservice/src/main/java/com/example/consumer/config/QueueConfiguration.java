package com.example.consumer.config;

import org.springframework.stereotype.Component;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Component("queueConfig")
public class QueueConfiguration {

    public String getQueueName() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            System.out.println("Initializing Replica with Hostname: " + hostname);

            if (hostname.contains("-")) {
                String[] parts = hostname.split("-");
                String slotStr = parts[parts.length - 1];
                
                int slot = Integer.parseInt(slotStr);
                int queueIndex = slot - 1; 

                return "sensor.queue." + queueIndex;
            }
        } catch (Exception e) {
            System.err.println("Failed to determine queue from hostname: " + e.getMessage());
        }

        return "sensor.queue.0";
    }
}