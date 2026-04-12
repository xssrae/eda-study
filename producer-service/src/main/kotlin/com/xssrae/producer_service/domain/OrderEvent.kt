package com.xssrae.producer_service.domain

data class OrderEvent(
    val orderId: String,
    val costumerId: String,
    val items: List<OrderItem>,
    val totalAmount: Double,
    val status: String,
    val timestamp: Long = System.currentTimeMillis()
)