package com.xssrae.producer_service.producer

import com.xssrae.producer_service.domain.OrderEvent
import com.xssrae.producer_service.domain.OrderItem
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import java.util.concurrent.CompletableFuture
import kotlin.test.Test

class OrderEventProducerTest {
    private val kafkaTemplate: KafkaTemplate<String, OrderEvent> = mockk()
    private val producer = OrderEventProducer(kafkaTemplate, "orders-topic")

    @Test
    fun `sendOrderEvent publishes with orderId as key`() = runTest {
        val event = OrderEvent(
            orderId = "order-1",
            costumerId = "customer-1",
            items = listOf(OrderItem("product-1", 1, 10.0)),
            totalAmount = 10.0,
            status = "PENDING"
        )
        every { kafkaTemplate.send("orders-topic", "order-1", event) } returns completedSend(event)

        producer.sendOrderEvent(event)

        verify(exactly = 1) { kafkaTemplate.send("orders-topic", "order-1", event) }
    }

    @Test
    fun `sendBatch publishes all events`() = runTest {
        val events = listOf(
            OrderEvent("order-1", "c-1", listOf(OrderItem("p-1", 1, 10.0)), 10.0, "PENDING"),
            OrderEvent("order-2", "c-2", listOf(OrderItem("p-2", 2, 15.0)), 30.0, "CONFIRMED")
        )
        events.forEach { event ->
            every { kafkaTemplate.send("orders-topic", event.orderId, event) } returns completedSend(event)
        }

        producer.sendBatch(events)

        verify(exactly = 1) { kafkaTemplate.send("orders-topic", "order-1", events[0]) }
        verify(exactly = 1) { kafkaTemplate.send("orders-topic", "order-2", events[1]) }
    }

    private fun completedSend(event: OrderEvent): CompletableFuture<SendResult<String, OrderEvent>> {
        val record = ProducerRecord("orders-topic", event.orderId, event)
        val metadata = org.apache.kafka.clients.producer.RecordMetadata(
            TopicPartition("orders-topic", 0),
            0L,
            0,
            System.currentTimeMillis(),
            0,
            0
        )
        return CompletableFuture.completedFuture(SendResult(record, metadata))
    }
}