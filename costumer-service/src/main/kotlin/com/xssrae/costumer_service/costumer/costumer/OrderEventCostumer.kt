package com.xssrae.costumer_service.costumer.costumer

import com.xssrae.costumer_service.costumer.service.S3StorageService
import com.xssrae.costumer_service.domain.OrderEvent
import com.xssrae.costumer_service.domain.OrderStatus
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
abstract class OrderEventCostumer (
    private val s3StorageService: S3StorageService
){
    private val log = LoggerFactory.getLogger(this::class.java)

    @KafkaListener(
        topics = ["\${kafka.topics.orders}"],
        groupId = "order-processor-group",
        concurrency = "3"               // 3 threads consumidoras em paralelo

    )
    fun consume(
        @Payload event: OrderEvent,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long
    ) {
        log.info("Recebido: orderId=${event.orderId} | partition=$partition | offset=$offset")
        val processingResult = when (event.status) {
            is OrderStatus.Pending -> processOrder(event)
            is OrderStatus.Confirmed -> confirmOrder(event)
            is OrderStatus.Failed -> {
                // Smart cast automático — status já é OrderStatus.Failed aqui
                log.warn("Pedido falhou: ${event.status.reason}")
                ProcessingResult.Skipped
            }

        }

        if (processingResult == ProcessingResult.Success) {
               runBlocking { s3StorageService.persistEvent(event) }
        }

    }

    private fun confirmOrder(event: OrderEvent): ProcessingResult {
        log.info("Confirmando pedido: orderId=${event.orderId}")
        // Lógica de confirmação do pedido
        return ProcessingResult.Success
    }

    abstract fun processOrder(event: OrderEvent): ProcessingResult

    enum class ProcessingResult {
        Success,
        Failed,
        Skipped
    }
}
