package com.aws.memento.domain

import java.time.LocalDateTime

data class Order(
    val id: String,
    val reviewId: String,
    val goodsType: String,
    val goodsName: String,
    val price: Int,
    val imageUrls: List<String>,
    val customerName: String,
    val customerPhone: String,
    val shippingAddress: ShippingAddress,
    val orderStatus: String,
    val createdAt: LocalDateTime,
)

data class ShippingAddress(
    val zipCode: String,
    val address: String,
    val detailAddress: String,
)

data class CreateOrderRequest(
    val reviewId: String,
    val goodsType: String,
    val goodsName: String,
    val price: Int,
    val imageUrls: List<String>,
    val customerName: String,
    val customerPhone: String,
    val shippingAddress: ShippingAddress,
    val orderStatus: String,
)
