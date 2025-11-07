package com.aws.memento.controller

import com.aws.memento.service.ReservationStorageService
import com.aws.memento.service.ReviewStorageService
import org.springframework.web.bind.annotation.*
import java.io.File

@RestController
@RequestMapping("/api/v1/demo")
@CrossOrigin(origins = ["http://localhost:9999"])
class DemoController(
    private val reservationStorageService: ReservationStorageService,
    private val reviewStorageService: ReviewStorageService
) {

    @PostMapping("/reset")
    fun resetDemoData(): Map<String, Any> {
        try {
            // 1. 모든 리뷰 파일 삭제
            val reviewsDir = File("reviews")
            var reviewsDeleted = 0
            if (reviewsDir.exists()) {
                reviewsDir.listFiles()?.forEach { file ->
                    if (file.isFile && file.extension == "json") {
                        file.delete()
                        reviewsDeleted++
                    }
                }
            }

            // 2. 예약 데이터 초기화 (기본 4개 예약으로 리셋)
            reservationStorageService.resetToDefaultReservations()

            return mapOf(
                "success" to true,
                "message" to "데모 데이터가 초기 상태로 리셋되었습니다.",
                "reviewsDeleted" to reviewsDeleted,
                "reservationsReset" to true
            )
        } catch (e: Exception) {
            return mapOf(
                "success" to false,
                "message" to "리셋 중 오류가 발생했습니다: ${e.message}"
            )
        }
    }

    @GetMapping("/status")
    fun getDemoStatus(): Map<String, Any> {
        try {
            // 현재 데모 상태 확인
            val parisReviewFile = File("reviews", "paris-night-tour.json")
            val hasParisReviews = parisReviewFile.exists()
            
            val defaultReservations = reservationStorageService.getReservationsBySessionId("default_session")
            val reviewableCount = defaultReservations.count { it.canReview }
            
            // 모든 리뷰 조회
            val allReviews = reviewStorageService.getReviewsBySessionId("default_session")

            return mapOf(
                "hasParisReviews" to hasParisReviews,
                "totalReservations" to defaultReservations.size,
                "reviewableReservations" to reviewableCount,
                "reservations" to defaultReservations.map { reservation ->
                    // 해당 예약과 연관된 리뷰 찾기
                    val relatedReview = allReviews.find { review ->
                        review.productName == reservation.packageName
                    }
                    
                    mapOf(
                        "id" to reservation.id,
                        "packageName" to reservation.packageName,
                        "status" to reservation.status,
                        "departureDate" to reservation.departureDate,
                        "canReview" to reservation.canReview,
                        "hasReview" to (relatedReview != null),
                        "review" to if (relatedReview != null) mapOf(
                            "id" to relatedReview.id,
                            "rating" to relatedReview.rating,
                            "publicReview" to relatedReview.publicReview,
                            "companion" to relatedReview.companion,
                            "createdAt" to relatedReview.createdAt
                        ) else null
                    )
                }
            )
        } catch (e: Exception) {
            return mapOf(
                "error" to "상태 확인 중 오류가 발생했습니다: ${e.message}"
            )
        }
    }
}