package com.example.consumer.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.UUID;

@Entity
public class Device {

    @Id
    private UUID id;

    private Double maxConsumption;
    private UUID userId;

    // No-args constructor (required by JPA)
    public Device() {
    }

    // All-args constructor
    public Device(UUID id, Double maxConsumption, UUID userId) {
        this.id = id;
        this.maxConsumption = maxConsumption;
        this.userId = userId;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Double getMaxConsumption() {
        return maxConsumption;
    }

    public void setMaxConsumption(Double maxConsumption) {
        this.maxConsumption = maxConsumption;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
