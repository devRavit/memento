package com.aws.memento.controller

import com.aws.memento.domain.CreateReviewRequest
import com.aws.memento.domain.Review
import com.aws.memento.service.OrderStorageService
import com.aws.memento.service.ReviewStorageService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

data class ReviewWithOrder(
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
    val orderId: String?,
)

@RestController
@RequestMapping("/api/v1/reviews")
class ReviewController(
    private val reviewStorageService: ReviewStorageService,
    private val orderStorageService: OrderStorageService,
) {
    @PostMapping
    fun createReview(
        @RequestBody request: CreateReviewRequest,
    ): ResponseEntity<Review> {
        val review = reviewStorageService.createReview(request)
        return ResponseEntity.ok(review)
    }

    @GetMapping("/product/{productId}")
    fun getReviewsByProductId(
        @PathVariable productId: String,
    ): ResponseEntity<List<Review>> {
        val reviews = reviewStorageService.getReviewsByProductId(productId)
        return ResponseEntity.ok(reviews)
    }

    @GetMapping("/session")
    fun getReviewsBySessionId(
        @RequestParam sessionId: String,
    ): ResponseEntity<List<ReviewWithOrder>> {
        val reviews = reviewStorageService.getReviewsBySessionId(sessionId)
        val allOrders = orderStorageService.getAllOrders()

        val reviewsWithOrder =
            reviews.map { review ->
                val order = allOrders.find { it.reviewId == review.id }
                ReviewWithOrder(
                    id = review.id,
                    sessionId = review.sessionId,
                    productId = review.productId,
                    productName = review.productName,
                    rating = review.rating,
                    privateReview = review.privateReview,
                    publicReview = review.publicReview,
                    companion = review.companion,
                    keywords = review.keywords,
                    imageUrls = review.imageUrls,
                    createdAt = review.createdAt,
                    orderId = order?.id,
                )
            }

        return ResponseEntity.ok(reviewsWithOrder)
    }

    @GetMapping
    fun getAllReviews(): ResponseEntity<List<Review>> {
        val reviews = reviewStorageService.getAllReviews()
        return ResponseEntity.ok(reviews)
    }
}
