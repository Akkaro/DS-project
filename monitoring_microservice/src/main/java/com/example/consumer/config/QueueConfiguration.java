package com.example.consumer.config;

import org.springframework.stereotype.Component;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Component("queueConfig")
public class QueueConfiguration {

    public String getQueueName() {
        try {
            // Get hostname. In Swarm, this will be "monitoring-1", "monitoring-2", etc.
            String hostname = InetAddress.getLocalHost().getHostName();
            System.out.println("Initializing Replica with Hostname: " + hostname);

            if (hostname.contains("-")) {
                String[] parts = hostname.split("-");
                String slotStr = parts[parts.length - 1]; // Get the last part
                
                // Swarm slots are usually 1-indexed (1, 2, 3...)
                // We want 0-indexed queues (sensor.queue.0, sensor.queue.1...)
                int slot = Integer.parseInt(slotStr);
                int queueIndex = slot - 1; 

                return "sensor.queue." + queueIndex;
            }
        } catch (Exception e) {
            System.err.println("Failed to determine queue from hostname: " + e.getMessage());
        }

        // Fallback for local testing (non-swarm)
        return "sensor.queue.0";
    }
}