package com.xssrae.producer_service

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka

@SpringBootTest(
    properties = [
        "app.kafka.topics.orders=orders-topic",
        "spring.kafka.bootstrap-servers=\${spring.embedded.kafka.brokers}"
    ]
)
@EmbeddedKafka(partitions = 1, topics = ["orders-topic"])
class ProducerServiceApplicationTests {

    @Test
    fun contextLoads() { }
}
