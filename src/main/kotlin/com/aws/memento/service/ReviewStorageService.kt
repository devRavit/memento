package com.aws.memento.service

import com.aws.memento.domain.CreateReviewRequest
import com.aws.memento.domain.Review
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.time.LocalDateTime
import java.util.UUID

@Service
class ReviewStorageService {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val objectMapper =
        ObjectMapper().apply {
            registerModule(JavaTimeModule())
        }

    private val reviewsDirectory = File("reviews")

    init {
        if (!reviewsDirectory.exists()) {
            reviewsDirectory.mkdirs()
            logger.info("리뷰 저장 디렉토리 생성: ${reviewsDirectory.absolutePath}")
        }
    }

    fun createReview(request: CreateReviewRequest): Review {
        val reviewId = "${request.sessionId}_${UUID.randomUUID()}"
        val review =
            Review(
                id = reviewId,
                sessionId = request.sessionId,
                productId = request.productId,
                productName = request.productName,
                rating = request.rating,
                privateReview = request.privateReview,
                publicReview = request.publicReview,
                companion = request.companion,
                keywords = request.keywords,
                imageUrls = request.imageUrls,
                createdAt = LocalDateTime.now(),
            )

        val productFile = File(reviewsDirectory, "${request.productId}.json")
        val reviews = if (productFile.exists()) {
            objectMapper.readValue<MutableList<Review>>(productFile)
        } else {
            mutableListOf()
        }

        reviews.add(review)
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(productFile, reviews)

        logger.info("리뷰 저장 완료: $reviewId (상품: ${request.productName})")
        return review
    }

    fun getReviewsByProductId(productId: String): List<Review> {
        val productFile = File(reviewsDirectory, "$productId.json")
        return if (productFile.exists()) {
            objectMapper.readValue(productFile)
        } else {
            emptyList()
        }
    }

    fun getReviewsBySessionId(sessionId: String): List<Review> {
        return reviewsDirectory
            .listFiles()
            ?.filter { it.extension == "json" }
            ?.flatMap { file ->
                val reviews: List<Review> = objectMapper.readValue(file)
                reviews.filter { it.sessionId == sessionId }
            } ?: emptyList()
    }

    fun getAllReviews(): List<Review> {
        return reviewsDirectory
            .listFiles()
            ?.filter { it.extension == "json" }
            ?.flatMap { file ->
                objectMapper.readValue<List<Review>>(file)
            } ?: emptyList()
    }
}
