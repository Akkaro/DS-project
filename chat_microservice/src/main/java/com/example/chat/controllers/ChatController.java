package com.example.chat.controllers;

import com.example.chat.dtos.ChatMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.List;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.model}")
    private String modelName;
    
    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage message) {
        
        if ("Admin".equalsIgnoreCase(message.getSender())) {
            messagingTemplate.convertAndSend("/topic/user/" + message.getUserId(), message);
            return; 
        }

        if (message.isChatWithAdmin()) {
            messagingTemplate.convertAndSend("/topic/admin", message);
            return;
        }

        String response = getRuleBasedResponse(message.getContent());

        if (response == null) {
            response = getAiResponse(message.getContent());
        }

        ChatMessage reply = new ChatMessage("Support Bot", response, message.getUserId(), false);
        messagingTemplate.convertAndSend("/topic/user/" + message.getUserId(), reply);
    }

    private String getRuleBasedResponse(String content) {
        if (content == null) return null;
        String lower = content.toLowerCase();

        if (lower.contains("hello") || lower.contains("hi")) 
            return "Hello! I am your automated support assistant.";
        if (lower.contains("login") || lower.contains("password")) 
            return "To reset your password, please visit the settings page.";
        if (lower.contains("device") || lower.contains("add")) 
            return "You can add new devices in the 'My Devices' tab.";
        if (lower.contains("bill") || lower.contains("cost")) 
            return "Billing is calculated based on hourly consumption. Check your charts.";

        return null; 
    }

    private String getAiResponse(String userMessage) {
        try {
            Map<String, Object> requestBody = Map.of(
                "model", modelName,
                "messages", List.of(
                    Map.of(
                        "role", "user", 
                        "content", "You are a helpful energy management assistant. Answer briefly: " + userMessage
                    )
                )
            );

            String authHeader = geminiApiKey.equals("unused") ? "" : "Bearer " + geminiApiKey;

            String jsonResponse = webClient.post()
                .uri(geminiApiUrl)
                .header("Content-Type", "application/json")
                .header("Authorization", authHeader)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            JsonNode root = objectMapper.readTree(jsonResponse);
            
            if (root.has("choices") && root.get("choices").size() > 0) {
                 return root.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();
            } else if (root.has("message")) {
                 return root.path("message")
                    .path("content")
                    .asText();
            }
            
            return "I received an empty response.";

        } catch (Exception e) {
            e.printStackTrace();
            return "I'm having trouble connecting to my AI brain. (Check API Key/Url)";
        }
    }
}