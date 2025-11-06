package com.aws.memento.controller

import com.aws.memento.domain.CreateOrderRequest
import com.aws.memento.domain.Order
import com.aws.memento.service.OrderStorageService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/orders")
class OrderController(
    private val orderStorageService: OrderStorageService,
) {
    @PostMapping
    fun createOrder(
        @RequestBody request: CreateOrderRequest,
    ): ResponseEntity<Order> {
        val order = orderStorageService.createOrder(request)
        return ResponseEntity.ok(order)
    }

    @GetMapping
    fun getAllOrders(): ResponseEntity<List<Order>> {
        val orders = orderStorageService.getAllOrders()
        return ResponseEntity.ok(orders)
    }

    @GetMapping("/{orderId}")
    fun getOrder(
        @PathVariable orderId: String,
    ): ResponseEntity<Order> {
        val order = orderStorageService.getOrderById(orderId)
        return if (order != null) {
            ResponseEntity.ok(order)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
