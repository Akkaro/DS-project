package com.example.consumer.repositories;

import com.example.consumer.entities.HourlyConsumption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HourlyConsumptionRepository extends JpaRepository<HourlyConsumption, Long> {
}