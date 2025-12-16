package com.example.loadbalancer.services;

import com.example.loadbalancer.dtos.SensorDataDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LoadBalancerService {

    private final RabbitTemplate rabbitTemplate;
    
    private static final int REPLICA_COUNT = 2; 

    public LoadBalancerService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "${app.queue.input}")
    public void distributeMessage(SensorDataDTO data) {
        UUID deviceId = data.getDeviceId();
        
        if (deviceId == null) {
            System.err.println("Received data with null Device ID. Dropping.");
            return;
        }

        int hash = Math.abs(deviceId.hashCode());
        int replicaIndex = hash % REPLICA_COUNT;
        
        String targetQueue = "sensor.queue." + replicaIndex;
        
        rabbitTemplate.convertAndSend(targetQueue, data);
        
        System.out.println("Routed Device " + deviceId + " -> " + targetQueue);
    }
}