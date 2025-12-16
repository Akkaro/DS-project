package com.example.demo.services;

import com.example.demo.config.RabbitConfig;
import com.example.demo.entities.Role;
import com.example.demo.entities.User;
import com.example.demo.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class AuthSyncConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuthSyncConsumer.class);
    private final UserRepository userRepository;

    public AuthSyncConsumer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @RabbitListener(queues = RabbitConfig.SYNC_QUEUE)
    public void receiveSyncMessage(Map<String, Object> message) {
        try {
            String action = (String) message.get("action");
            
            if ("create_user".equals(action)) {
                String userIdStr = (String) message.get("userId");
                String username = (String) message.get("username");
                String name = (String) message.get("name");
                String email = (String) message.get("email");
                String roleStr = (String) message.get("role");

                if (userIdStr != null && username != null) {
                    UUID userId = UUID.fromString(userIdStr);
                    
                    if (!userRepository.existsById(userId)) {
                        User user = new User(
                            userId, 
                            username, 
                            name, 
                            email, 
                            Role.valueOf(roleStr)
                        );
                        userRepository.save(user);
                        log.info("Synced new user from Auth Service: {}", username);
                    } else {
                        log.debug("User {} already exists, skipping sync.", username);
                    }
                }
            } else if ("delete_user".equals(action)) {
                String userIdStr = (String) message.get("userId");
                if (userIdStr != null) {
                    UUID userId = UUID.fromString(userIdStr);
                    if (userRepository.existsById(userId)) {
                        userRepository.deleteById(userId);
                        log.info("Synced deletion: Removed profile for user ID {}", userId);
                    } else {
                        log.warn("Synced deletion: User profile not found for ID {}", userId);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error processing user sync message", e);
        }
    }
}