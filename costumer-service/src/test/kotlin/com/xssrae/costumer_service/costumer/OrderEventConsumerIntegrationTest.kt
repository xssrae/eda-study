package com.xssrae.costumer_service.costumer

import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.xssrae.costumer_service.domain.OrderEvent
import com.xssrae.costumer_service.domain.OrderItem
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import java.time.Duration
import java.util.UUID
import kotlin.test.assertEquals

@SpringBootTest(
    properties = [
        "app.kafka.topics.orders=orders-topic",
        "spring.kafka.bootstrap-servers=\${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
    ]
)
@EmbeddedKafka(partitions = 1, topics = ["orders-topic"])
class OrderEventConsumerIntegrationTest {

    @Autowired
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

    private var verifyConsumer: org.apache.kafka.clients.consumer.Consumer<String, String>? = null

    private val objectMapper = jacksonObjectMapper()

    @AfterEach
    fun tearDown() {
        verifyConsumer?.close()
        verifyConsumer = null
    }

    @Test
    fun `listener consumes a kafka event`() {
        val consumerProps =
            KafkaTestUtils.consumerProps(
                "integration-verify-${UUID.randomUUID()}",
                "true",
                embeddedKafkaBroker,
            )
        consumerProps[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        consumerProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        consumerProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java

        verifyConsumer =
            DefaultKafkaConsumerFactory<String, String>(consumerProps).createConsumer()

        verifyConsumer!!.subscribe(listOf("orders-topic"))
        while (verifyConsumer!!.assignment().isEmpty()) {
            verifyConsumer!!.poll(Duration.ofMillis(100))
        }
        verifyConsumer!!.seekToEnd(verifyConsumer!!.assignment())
        verifyConsumer!!.poll(Duration.ZERO)

        val producerProps =
            mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to embeddedKafkaBroker.brokersAsString,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            )
        val producerFactory =
            DefaultKafkaProducerFactory(
                producerProps,
                StringSerializer(),
                JsonSerializer<OrderEvent>(objectMapper).apply { setAddTypeInfo(false) },
            )
        val kafkaTemplate = KafkaTemplate(producerFactory)

        val event =
            OrderEvent(
                orderId = "order-consumer-it",
                costumerId = "customer-consumer-it",
                items = listOf(OrderItem("product-1", 1, 22.0)),
                totalAmount = 22.0,
                status = "PENDING",
            )

        kafkaTemplate.send("orders-topic", event.orderId, event).get()

        val deadline = System.currentTimeMillis() + 15_000
        var parsed: OrderEvent? = null
        while (parsed == null && System.currentTimeMillis() < deadline) {
            val polled = verifyConsumer!!.poll(Duration.ofMillis(400))
            if (polled.isEmpty) continue
            val record = polled.iterator().asSequence().first()
            parsed = objectMapper.readValue<OrderEvent>(record.value())
            break
        }
        checkNotNull(parsed) { "Nenhum OrderEvent foi lido do tópico dentro do tempo limite" }

        assertEquals("order-consumer-it", parsed.orderId)
        assertEquals("PENDING", parsed.status)
    }
}
