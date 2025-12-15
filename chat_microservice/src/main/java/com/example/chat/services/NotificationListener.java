package com.example.chat.services;

import com.example.chat.dtos.NotificationDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationListener {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = "${app.queue.notification}")
    public void handleNotification(NotificationDTO notification) {
        System.out.println("Received notification for user: " + notification.getUserId());
        
        // Push notification to the specific user's topic
        // Frontend must subscribe to: /topic/user/{userId}
        messagingTemplate.convertAndSend(
            "/topic/user/" + notification.getUserId(), 
            "NOTIFICATION: " + notification.getMessage()
        );
    }
}