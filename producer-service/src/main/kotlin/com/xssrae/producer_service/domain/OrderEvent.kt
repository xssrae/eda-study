package com.xssrae.producer_service.domain

class OrderEvent (
    val orderId: String,
    val costumerId: String,
    val items: List<OrderItem>,
    val totalAmount: Double,
    val status: String,
    val timestamp: Long = System.currentTimeMillis()
)
    data class OrderItem(
    val productId: String,
    val quantity: Int,
    val price: Double

) sealed class OrderStatus {
    object Pending : OrderStatus()
    object Confirmed : OrderStatus()
    data class Failed(val reason: String) : OrderStatus()

    companion object {
        fun fromString(value: String): OrderStatus = when (value) {
            "PENDING" -> Pending
            "CONFIRMED" -> Confirmed
            else -> Failed("Unknown status: $value")

        }
    }
}
