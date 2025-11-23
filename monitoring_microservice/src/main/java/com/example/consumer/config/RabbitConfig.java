package com.example.consumer.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String QUEUE_NAME = "sensor.data.queue";

    @Bean
    public Queue sensorDataQueue() {
        return new Queue(QUEUE_NAME, true);
    }
}
