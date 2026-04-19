package com.xssrae.producer_service.producer

import com.xssrae.producer_service.domain.OrderEvent
import com.xssrae.producer_service.domain.OrderItem
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

@SpringBootTest(
    properties = [
        "app.kafka.topics.orders=orders-topic",
        "spring.kafka.bootstrap-servers=\${spring.embedded.kafka.brokers}"
    ]
)
@EmbeddedKafka(partitions = 1, topics = ["orders-topic"])
class OrderEventProducerIntegrationsTest {
    @Autowired
    private lateinit var orderEventProducer: OrderEventProducer

    @Autowired
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

    private lateinit var consumer: org.apache.kafka.clients.consumer.Consumer<String, OrderEvent>

    @BeforeEach
    fun setUp() {
        consumer = createConsumer()
    }

    @AfterEach
    fun tearDown() {
        consumer.close()
    }

    @Test
    fun `sendOrderEvent publishes message to kafka topic`() = runTest {
        val event = OrderEvent(
            orderId = "order-it-1",
            costumerId = "customer-it-1",
            items = listOf(OrderItem("product-1", 1, 35.0)),
            totalAmount = 35.0,
            status = "PENDING"
        )
        consumer.subscribe(listOf("orders-topic"))

        orderEventProducer.sendOrderEvent(event)

        val received = KafkaTestUtils.getSingleRecord(consumer, "orders-topic")
        assertEquals("order-it-1", received.key())
        assertEquals("order-it-1", received.value().orderId)
        assertEquals("PENDING", received.value().status)
    }

    private fun createConsumer(): org.apache.kafka.clients.consumer.Consumer<String, OrderEvent> {
        val consumerProps = KafkaTestUtils.consumerProps("producer-it-group", "true", embeddedKafkaBroker)
        consumerProps[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        val valueDeserializer = JsonDeserializer(OrderEvent::class.java).apply {
            addTrustedPackages("com.xssrae.producer_service.domain")
            setUseTypeMapperForKey(false)
        }
        val consumerFactory = DefaultKafkaConsumerFactory(
            consumerProps,
            StringDeserializer(),
            valueDeserializer
        )
        return consumerFactory.createConsumer()
    }
}