package com.xssrae.producer_service.controller

import com.xssrae.producer_service.domain.OrderItem
import com.xssrae.producer_service.producer.OrderEventProducer
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.slot
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OrderControllerTest {

    private val producer: OrderEventProducer = mockk()
    private val controller = OrderController(producer)

    @Test
    fun `createOrder maps request and returns success message`() = runTest {
        val capturedEvent = slot<com.xssrae.producer_service.domain.OrderEvent>()
        val request = CreateOrderRequest(
            orderId = "order-1",
            costumerId = "customer-1",
            items = listOf(OrderItem("product-1", 2, 9.9)),
            totalAmount = 19.8,
            status = "PENDING"
        )
        coEvery { producer.sendOrderEvent(any()) } returns Unit

        val response = controller.createOrder(request)

        assertEquals(200, response.statusCode.value())
        assertEquals("Evento de pedido enviado com sucesso", response.body?.get("message"))
        coVerify(exactly = 1) {
            producer.sendOrderEvent(capture(capturedEvent))
        }
        assertEquals("order-1", capturedEvent.captured.orderId)
        assertEquals("customer-1", capturedEvent.captured.costumerId)
        assertEquals(listOf(OrderItem("product-1", 2, 9.9)), capturedEvent.captured.items)
        assertEquals(19.8, capturedEvent.captured.totalAmount)
        assertEquals("PENDING", capturedEvent.captured.status)
    }
}