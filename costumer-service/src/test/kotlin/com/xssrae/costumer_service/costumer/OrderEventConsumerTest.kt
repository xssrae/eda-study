package com.xssrae.costumer_service.costumer

import com.xssrae.costumer_service.costumer.costumer.OrderEventCostumer
import com.xssrae.costumer_service.domain.OrderEvent
import com.xssrae.costumer_service.domain.OrderItem
import kotlin.test.Test

class OrderEventConsumerTest {
    private val consumer = OrderEventCostumer()

    @Test
    fun `consume processes pending status without exceptions`() {
        val event = OrderEvent(
            orderId = "order-1",
            costumerId = "customer-1",
            items = listOf(OrderItem("product-1", 1, 10.0)),
            totalAmount = 10.0,
            status = "PENDING"
        )

        consumer.consume(event, 0, 1L)
    }

    @Test
    fun `consume handles unknown status without exceptions`() {
        val event = OrderEvent(
            orderId = "order-2",
            costumerId = "customer-2",
            items = listOf(OrderItem("product-2", 2, 15.0)),
            totalAmount = 30.0,
            status = "INVALID"
        )

        consumer.consume(event, 0, 2L)
    }
}