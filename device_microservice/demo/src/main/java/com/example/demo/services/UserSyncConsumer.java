package com.example.demo.services;

import com.example.demo.config.RabbitConfig;
import com.example.demo.entities.User;
import com.example.demo.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class UserSyncConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserSyncConsumer.class);
    private final UserRepository userRepository;

    public UserSyncConsumer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @RabbitListener(queues = RabbitConfig.SYNC_QUEUE)
    public void receiveSyncMessage(Map<String, Object> message) {
        try {
            String action = (String) message.get("action");
            
            if ("create_user".equals(action)) {
                String userIdStr = (String) message.get("userId");
                if (userIdStr != null) {
                    UUID userId = UUID.fromString(userIdStr);
                    LOGGER.info("Received sync event: User created with ID {}", userId);
                    
                    if (!userRepository.existsById(userId)) {
                        userRepository.save(new User(userId));
                        LOGGER.debug("User {} saved to local database.", userId);
                    } else {
                        LOGGER.debug("User {} already exists locally.", userId);
                    }
                }

            } else if ("delete_user".equals(action)) {
                String userIdStr = (String) message.get("userId");
                if (userIdStr != null) {
                    UUID userId = UUID.fromString(userIdStr);
                    LOGGER.info("Received sync event: User deleted with ID {}", userId);
                    
                    if (userRepository.existsById(userId)) {
                        userRepository.deleteById(userId);
                        LOGGER.debug("User {} deleted from local database.", userId);
                    } else {
                        LOGGER.warn("User {} not found locally, skipping delete.", userId);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error processing user sync message", e);
        }
    }
}