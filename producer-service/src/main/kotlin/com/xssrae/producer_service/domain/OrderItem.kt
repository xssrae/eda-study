package com.xssrae.producer_service.domain

data class OrderItem(
    val productId: String,
    val quantity: Int,
    val price: Double
)