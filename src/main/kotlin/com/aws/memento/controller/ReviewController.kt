package com.aws.memento.controller

import com.aws.memento.domain.CreateReviewRequest
import com.aws.memento.domain.Review
import com.aws.memento.service.ReviewStorageService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/reviews")
class ReviewController(
    private val reviewStorageService: ReviewStorageService,
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
    ): ResponseEntity<List<Review>> {
        val reviews = reviewStorageService.getReviewsBySessionId(sessionId)
        return ResponseEntity.ok(reviews)
    }

    @GetMapping
    fun getAllReviews(): ResponseEntity<List<Review>> {
        val reviews = reviewStorageService.getAllReviews()
        return ResponseEntity.ok(reviews)
    }
}
