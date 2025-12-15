package com.example.consumer.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("#{queueConfig.getQueueName()}")
    private String sensorQueue;

    // Unique queue for monitoring sync
    public static final String SYNC_QUEUE = "monitoring.sync.queue";
    public static final String EXCHANGE_NAME = "internal.exchange";

    @Bean
    public Queue sensorDataQueue() {
        return new Queue(sensorQueue, true);
    }

    // Queue for sending alerts to the Chat Service
    @Bean
    public Queue notificationQueue() {
        return new Queue("notification.queue", true);
    }

    @Bean
    public Queue syncQueue() {
        return new Queue(SYNC_QUEUE, true);
    }

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(Queue syncQueue, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(syncQueue).to(fanoutExchange);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}