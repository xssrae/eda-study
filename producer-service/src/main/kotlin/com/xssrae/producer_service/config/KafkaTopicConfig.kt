package com.xssrae.producer_service.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaTopicConfig {

    @Bean
    fun ordersTopic(): NewTopic =
        TopicBuilder.name("orders-topic")
            .partitions(1)
            .replicas(1)
            .build()
}