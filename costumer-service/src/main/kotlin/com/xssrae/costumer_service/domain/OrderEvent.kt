// consumer-service/domain/OrderEvent.kt
package com.xssrae.costumer_service.domain

data class OrderEvent(
    val orderId: String,
    val costumerId: String,
    val items: List<OrderItem>,
    val totalAmount: Double,
    val status: String,
    val timestamp: Long = 0L
)

data class OrderItem(
    val productId: String,
    val quantity: Int,
    val price: Double
)