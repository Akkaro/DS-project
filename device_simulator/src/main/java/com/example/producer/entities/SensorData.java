package com.example.producer.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SensorData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String timestamp;
    private double measurement;
    private String sensorId;

    public SensorData() {}

    public SensorData(double measurement) {
        this.timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.measurement = measurement;
        this.sensorId = "6d5be097-fbbc-4a2c-a055-2973e2580134";
    }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public double getMeasurement() { return measurement; }
    public void setMeasurement(double measurement) { this.measurement = measurement; }
    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }
}
