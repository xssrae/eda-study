package com.xssrae.costumer_service.domain

sealed class OrderStatus {
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
