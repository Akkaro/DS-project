package com.example.consumer.repositories;


import com.example.consumer.entities.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {}

