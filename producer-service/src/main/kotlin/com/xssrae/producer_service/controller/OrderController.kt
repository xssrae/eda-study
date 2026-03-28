package com.xssrae.producer_service.controller

import com.xssrae.producer_service.domain.OrderEvent
import com.xssrae.producer_service.domain.OrderItem
import com.xssrae.producer_service.producer.OrderEventProducer
import org.apache.coyote.Response
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
class OrderController ( private val producer: OrderEventProducer) {

    @PostMapping
    suspend fun createOrder(@RequestBody request: CreateOrderRequest): ResponseEntity<Map<String, String>> {
        val event = OrderEvent(
            orderId = request.orderId,
            costumerId = request.costumerId,
            items = request.items,
            totalAmount = request.totalAmount,
            status = request.status
        )
        producer.sendOrderEvent(event)
        return ResponseEntity.ok(mapOf("message" to "Evento de pedido enviado com sucesso"))
    }
}

data class CreateOrderRequest(
    val orderId: String,
    val costumerId: String,
    val items: List<OrderItem>,
    val totalAmount: Double,
    val status: String
)