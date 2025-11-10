package com.example.demo.handlers.exceptions.model;

import java.util.Collection;
import java.util.Date;

// Copied from user-service
public class ExceptionHandlerResponseDTO {
    private Date timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private String resource;
    private Collection<?> details;

    public ExceptionHandlerResponseDTO(String resource, String error, int status, String message, Collection<?> details, String path) {
        this.timestamp = new Date();
        this.resource = resource;
        this.error = error;
        this.status = status;
        this.message = message;
        this.details = details;
        this.path = path;
    }
    
    // Getters and Setters
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }
    public Collection<?> getDetails() { return details; }
    public void setDetails(Collection<?> details) { this.details = details; }
}