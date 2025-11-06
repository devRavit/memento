package com.aws.memento.domain

import java.time.LocalDateTime

data class Review(
    val id: String,
    val sessionId: String,
    val productId: String,
    val productName: String,
    val rating: Int,
    val privateReview: String?,
    val publicReview: String,
    val companion: String?,
    val keywords: List<String>,
    val imageUrls: List<String>,
    val createdAt: LocalDateTime,
)

data class CreateReviewRequest(
    val sessionId: String,
    val productId: String,
    val productName: String,
    val rating: Int,
    val privateReview: String?,
    val publicReview: String,
    val companion: String?,
    val keywords: List<String>,
    val imageUrls: List<String>,
)
