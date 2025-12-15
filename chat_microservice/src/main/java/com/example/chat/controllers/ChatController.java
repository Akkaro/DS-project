package com.example.chat.controllers;

import com.example.chat.dtos.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
// import org.springframework.web.reactive.function.client.WebClient; // Uncomment for Real AI

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage message) {
        
        // CASE 1: Admin replying to a specific user
        if ("Admin".equalsIgnoreCase(message.getSender())) {
            // Admin messages are directed to specific users
            messagingTemplate.convertAndSend("/topic/user/" + message.getUserId(), message);
            return; 
        }

        // CASE 2: User wants to talk to Admin
        if (message.isChatWithAdmin()) {
            // Forward to Admin Topic (All admins see this)
            messagingTemplate.convertAndSend("/topic/admin", message);
            // Optionally: Send an auto-ack to user
            // sendSystemReply(message.getUserId(), "An admin has been notified.");
            return;
        }

        // CASE 3: User wants to talk to Support Bot (Rule + AI)
        String response = getRuleBasedResponse(message.getContent());

        if (response == null) {
            // No rule matched -> Fallback to AI
            response = getAiResponse(message.getContent());
        }

        // Send Bot Reply to User
        ChatMessage reply = new ChatMessage("Support Bot", response, message.getUserId(), false);
        messagingTemplate.convertAndSend("/topic/user/" + message.getUserId(), reply);
    }

    // --- 1. RULE ENGINE ---
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

        return null; // No rule matched
    }

    // --- 2. AI ENGINE (Gemini Placeholder) ---
    private String getAiResponse(String content) {
        // TODO: Replace with actual Gemini API call
        // String apiKey = "...";
        // Call https://generativelanguage.googleapis.com/...
        
        return "I couldn't find a specific rule for that. As an AI, I suggest checking the FAQs or switching to Admin mode.";
    }
}