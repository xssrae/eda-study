package com.xssrae.producer_service.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class OrderStatusTest {

    @Test
    fun `fromString returns Pending for PENDING`() {
        val result = OrderStatus.fromString("PENDING")
        assertIs<OrderStatus.Pending>(result)
    }

    @Test
    fun `fromString returns Confirmed for CONFIRMED`() {
        val result = OrderStatus.fromString("CONFIRMED")
        assertIs<OrderStatus.Confirmed>(result)
    }

    @Test
    fun `fromString returns Failed for unknown values`() {
        val result = OrderStatus.fromString("INVALID")
        val failed = assertIs<OrderStatus.Failed>(result)
        assertEquals("Unknown status: INVALID", failed.reason)
    }
}