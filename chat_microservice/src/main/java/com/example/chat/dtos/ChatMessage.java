package com.example.chat.dtos;

public class ChatMessage {
    private String sender;
    private String content;
    private String userId;
    private boolean chatWithAdmin;

    public ChatMessage() {}

    public ChatMessage(String sender, String content, String userId, boolean chatWithAdmin) {
        this.sender = sender;
        this.content = content;
        this.userId = userId;
        this.chatWithAdmin = chatWithAdmin;
    }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public boolean isChatWithAdmin() { return chatWithAdmin; }
    public void setChatWithAdmin(boolean chatWithAdmin) { this.chatWithAdmin = chatWithAdmin; }
}