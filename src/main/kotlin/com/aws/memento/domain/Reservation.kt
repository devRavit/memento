package com.aws.memento.domain

import java.time.LocalDateTime

data class Reservation(
    val id: String,
    val sessionId: String,
    val date: String,
    val dayOfWeek: String,
    val reservationNumber: String,
    val status: String, // 'completed', 'canceled'
    val packageName: String,
    val packageImage: String,
    val packageType: String,
    val departureDate: String,
    val returnDate: String,
    val days: Int,
    val departureTime: String,
    val returnTime: String,
    val travelers: String,
    val airline: String,
    val canReview: Boolean = false,
    val reviewDaysLeft: Int = 0,
    val createdAt: LocalDateTime,
)

data class CreateReservationRequest(
    val sessionId: String,
    val date: String,
    val dayOfWeek: String,
    val reservationNumber: String,
    val status: String,
    val packageName: String,
    val packageImage: String,
    val packageType: String,
    val departureDate: String,
    val returnDate: String,
    val days: Int,
    val departureTime: String,
    val returnTime: String,
    val travelers: String,
    val airline: String,
    val canReview: Boolean = false,
    val reviewDaysLeft: Int = 0,
)