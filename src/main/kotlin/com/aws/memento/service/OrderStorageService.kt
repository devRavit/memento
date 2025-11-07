package com.aws.memento.service

import com.aws.memento.domain.CreateOrderRequest
import com.aws.memento.domain.Order
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.time.LocalDateTime
import java.util.UUID

@Service
class OrderStorageService {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val objectMapper =
        ObjectMapper().apply {
            registerModule(JavaTimeModule())
            registerModule(KotlinModule.Builder().build())
        }

    private val ordersDirectory = File("orders")

    init {
        if (!ordersDirectory.exists()) {
            ordersDirectory.mkdirs()
            logger.info("주문 저장 디렉토리 생성: ${ordersDirectory.absolutePath}")
        }
    }

    fun createOrder(request: CreateOrderRequest): Order {
        val now = LocalDateTime.now()
        val orderId = "ORD${now.toEpochSecond(java.time.ZoneOffset.UTC).toString().takeLast(8)}"

        val trackingNumber = generateTrackingNumber()
        val courierCompany = "CJ대한통운"
        val estimatedDeliveryDate = now.plusDays((2..3).random().toLong())

        val order =
            Order(
                id = orderId,
                reviewId = request.reviewId,
                goodsType = request.goodsType,
                goodsName = request.goodsName,
                price = request.price,
                imageUrls = request.imageUrls,
                customerName = request.customerName,
                customerPhone = request.customerPhone,
                shippingAddress = request.shippingAddress,
                orderStatus = "배송중",
                trackingNumber = trackingNumber,
                courierCompany = courierCompany,
                estimatedDeliveryDate = estimatedDeliveryDate,
                createdAt = now,
            )

        val orderFile = File(ordersDirectory, "$orderId.json")
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(orderFile, order)

        logger.info("주문 저장 완료: $orderId (굿즈: ${request.goodsName}, 송장번호: $trackingNumber)")
        return order
    }

    private fun generateTrackingNumber(): String {
        val randomDigits = (1..12).map { (0..9).random() }.joinToString("")
        return randomDigits
    }

    fun getOrderById(orderId: String): Order? {
        val orderFile = File(ordersDirectory, "$orderId.json")
        return if (orderFile.exists()) {
            objectMapper.readValue(orderFile)
        } else {
            null
        }
    }

    fun getAllOrders(): List<Order> {
        return ordersDirectory
            .listFiles()
            ?.filter { it.extension == "json" }
            ?.map { file ->
                objectMapper.readValue<Order>(file)
            } ?: emptyList()
    }
}
