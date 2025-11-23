package com.example.consumer.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${app.queue.sensor}")
    private String sensorQueue;

    @Value("${app.queue.sync}")
    private String syncQueue;

    @Bean
    public Queue sensorDataQueue() {
        return new Queue(sensorQueue, true);
    }

    @Bean
    public Queue syncQueue() {
        return new Queue(syncQueue, true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}