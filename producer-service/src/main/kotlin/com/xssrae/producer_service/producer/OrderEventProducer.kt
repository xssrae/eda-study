package com.xssrae.producer_service.producer

import com.xssrae.producer_service.domain.OrderEvent
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class OrderEventProducer (
    private val KafkaTemplate: KafkaTemplate<String, OrderEvent>,
    @Value("\${kafka.topics.orders}") private val topic: String
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    suspend fun sendOrderEvent(event: OrderEvent) {
        log.info("Enviando evento: orderId=${event.orderId}, status=${event.status}")

        KafkaTemplate
            .send(topic, event.orderId, event)
            .await()
            .also { result ->
                log.info("Evento enviado ✓ | partition=${result.recordMetadata.partition()}, offset=${result.recordMetadata.offset()}")
            }
    }

    suspend fun sendBatch(events: List<OrderEvent>) = coroutineScope {
        events
            .map { event -> async { sendOrderEvent(event) } }
            .awaitAll()
            .also { results ->
                log.info("Lote enviado: ${results.size} eventos ✓")
            }
    }
}
