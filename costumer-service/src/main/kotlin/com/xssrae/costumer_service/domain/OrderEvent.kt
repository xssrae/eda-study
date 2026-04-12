package com.xssrae.costumer_service.domain

class OrderEvent (
    val orderId: String,
    val costumerId: String,
    val items: List<OrderItem>,
    val totalAmount: Double,
    val status: OrderStatus,
    val timestamp: Long = System.currentTimeMillis()
)

