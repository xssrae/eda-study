package com.xssrae.costumer_service.costumer.costumer

import com.xssrae.costumer_service.domain.OrderEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class OrderEventCostumer {

    private val log = LoggerFactory.getLogger(this::class.java)

    @KafkaListener(
        topics = ["\${app.kafka.topics.orders}"],
        groupId = "order-processor-group"
    )
    fun consume(
        @Payload event: OrderEvent,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long
    ) {
        log.info("✅ Evento recebido!")
        log.info("   orderId   = ${event.orderId}")
        log.info("   costumerId= ${event.costumerId}")
        log.info("   status    = ${event.status}")
        log.info("   total     = ${event.totalAmount}")
        log.info("   partition = $partition | offset = $offset")

        // ✅ when em String — bate com data class OrderEvent(status: String)
        when (event.status) {
            "PENDING"   -> log.info("📦 Processando pedido ${event.orderId}")
            "CONFIRMED" -> log.info("✔️  Pedido ${event.orderId} confirmado")
            else        -> log.warn("⚠️  Status desconhecido: ${event.status}")
        }
    }
}