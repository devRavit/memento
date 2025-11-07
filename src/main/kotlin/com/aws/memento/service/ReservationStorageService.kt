package com.aws.memento.service

import com.aws.memento.domain.CreateReservationRequest
import com.aws.memento.domain.Reservation
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
class ReservationStorageService {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val objectMapper =
        ObjectMapper().apply {
            registerModule(JavaTimeModule())
            registerModule(KotlinModule.Builder().build())
        }

    private val reservationsDirectory = File("reservations")

    init {
        if (!reservationsDirectory.exists()) {
            reservationsDirectory.mkdirs()
            logger.info("예약 저장 디렉토리 생성: ${reservationsDirectory.absolutePath}")
            initializeDefaultReservations()
        }
    }

    private fun initializeDefaultReservations() {
        // 기본 예약 데이터 생성 (날짜순 역정렬: 최신순)
        val defaultReservations = listOf(
            // 1. 미래 예약 (예약 확정)
            Reservation(
                id = "RES_001",
                sessionId = "default_session",
                date = "2025.12.15",
                dayOfWeek = "일",
                reservationNumber = "25121510341601108",
                status = "confirmed",
                packageName = "오사카 유니버설 스튜디오 패키지",
                packageImage = "https://images.unsplash.com/photo-1578662996442-48f60103fc96?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80",
                packageType = "테마파크 | 자유여행",
                departureDate = "2025.12.15 (일)",
                returnDate = "2025.12.17 (화)",
                days = 3,
                departureTime = "09:30",
                returnTime = "20:15",
                travelers = "성인 2명",
                airline = "진에어 LJ201",
                canReview = false,
                reviewDaysLeft = 0,
                createdAt = LocalDateTime.now(),
            ),
            // 2. 최근 완료 여행 (리뷰 작성 가능)
            Reservation(
                id = "RES_002",
                sessionId = "default_session",
                date = "2025.11.01",
                dayOfWeek = "금",
                reservationNumber = "25110110341601109",
                status = "completed",
                packageName = "파리 노트르담+에펠탑+센느강 야경 투어",
                packageImage = "https://media.triple.guide/triple-cms/c_limit,f_auto,h_1024,w_1024/9ded49d6-544c-4d4e-812a-3e0ed3c50b65.jpeg",
                packageType = "가이드 투어 | 소수인원 야경 투어",
                departureDate = "2025.11.01 (금)",
                returnDate = "2025.11.01 (금)",
                days = 1,
                departureTime = "17:50",
                returnTime = "20:50",
                travelers = "성인 2명",
                airline = "현지 투어 (한국어 가이드)",
                canReview = true,
                reviewDaysLeft = 24,
                createdAt = LocalDateTime.now(),
            ),
            // 3. 최근 완료 여행 (리뷰 작성 가능)
            Reservation(
                id = "RES_003",
                sessionId = "default_session",
                date = "2025.10.20",
                dayOfWeek = "일",
                reservationNumber = "25102010341601110",
                status = "completed",
                packageName = "제주도 한라산 등반 투어",
                packageImage = "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80",
                packageType = "트래킹 투어 | 소그룹",
                departureDate = "2025.10.20 (일)",
                returnDate = "2025.10.22 (화)",
                days = 3,
                departureTime = "06:00",
                returnTime = "18:00",
                travelers = "성인 2명",
                airline = "김포-제주 항공편",
                canReview = true,
                reviewDaysLeft = 15,
                createdAt = LocalDateTime.now(),
            ),
            // 4. 과거 완료 여행 (리뷰 작성 완료)
            Reservation(
                id = "RES_004",
                sessionId = "default_session",
                date = "2025.09.14",
                dayOfWeek = "토",
                reservationNumber = "25091410341601111",
                status = "completed",
                packageName = "부산 해운대 해양 체험",
                packageImage = "https://images.unsplash.com/photo-1544551763-46a013bb70d5?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1000&q=80",
                packageType = "해양 레저 | 체험형",
                departureDate = "2025.09.14 (토)",
                returnDate = "2025.09.15 (일)",
                days = 2,
                departureTime = "08:00",
                returnTime = "17:00",
                travelers = "성인 2명",
                airline = "KTX + 현지 교통",
                canReview = false,
                reviewDaysLeft = 0,
                createdAt = LocalDateTime.now(),
            )
        )

        val sessionFile = File(reservationsDirectory, "default_session.json")
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(sessionFile, defaultReservations)
        
        // 파리 투어 리뷰 초기화 (서버 시작시마다 새로 테스트 가능하도록)
        val parisReviewFile = File("reviews", "paris-night-tour.json")
        if (parisReviewFile.exists()) {
            parisReviewFile.delete()
            logger.info("파리 투어 리뷰 파일 초기화 완료")
        }
        
        // 이미 리뷰가 작성된 예약들에 대한 더미 리뷰 데이터 생성
        initializeDummyReviews()
        
        logger.info("기본 예약 데이터 초기화 완료 (총 ${defaultReservations.size}개 예약)")
    }

    private fun initializeDummyReviews() {
        val reviewsDirectory = File("reviews")
        if (!reviewsDirectory.exists()) {
            reviewsDirectory.mkdirs()
        }

        // 부산 해양 체험 리뷰 (이미 작성 완료된 상태)
        val busanReviews = listOf(
            mapOf(
                "id" to "default_session_busan_001",
                "sessionId" to "default_session",
                "productId" to "busan-marine",
                "productName" to "부산 해운대 해양 체험",
                "rating" to 5,
                "privateReview" to null,
                "publicReview" to "해운대 바다에서 즐긴 스노클링과 제트스키가 정말 짜릿했어요! 날씨도 좋고 바다도 깨끗해서 최고였습니다. 가족 모두 만족하는 여행이었어요.",
                "companion" to "family",
                "keywords" to listOf("액티비티가 다양해요", "경치가 아름다워요"),
                "imageUrls" to listOf<String>(),
                "createdAt" to "2025-09-20T18:30:00"
            )
        )
        
        val busanFile = File(reviewsDirectory, "busan-marine.json")
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(busanFile, busanReviews)
        
        logger.info("더미 리뷰 데이터 생성 완료")
    }

    fun resetToDefaultReservations() {
        initializeDefaultReservations()
        logger.info("예약 데이터가 기본 상태로 리셋되었습니다")
    }

    fun createReservation(request: CreateReservationRequest): Reservation {
        val reservationId = "${request.sessionId}_${UUID.randomUUID()}"
        val reservation = Reservation(
            id = reservationId,
            sessionId = request.sessionId,
            date = request.date,
            dayOfWeek = request.dayOfWeek,
            reservationNumber = request.reservationNumber,
            status = request.status,
            packageName = request.packageName,
            packageImage = request.packageImage,
            packageType = request.packageType,
            departureDate = request.departureDate,
            returnDate = request.returnDate,
            days = request.days,
            departureTime = request.departureTime,
            returnTime = request.returnTime,
            travelers = request.travelers,
            airline = request.airline,
            canReview = request.canReview,
            reviewDaysLeft = request.reviewDaysLeft,
            createdAt = LocalDateTime.now(),
        )

        val sessionFile = File(reservationsDirectory, "${request.sessionId}.json")
        val reservations = if (sessionFile.exists()) {
            objectMapper.readValue<MutableList<Reservation>>(sessionFile)
        } else {
            mutableListOf()
        }

        reservations.add(reservation)
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(sessionFile, reservations)

        logger.info("예약 저장 완료: $reservationId (상품: ${request.packageName})")
        return reservation
    }

    fun getReservationsBySessionId(sessionId: String): List<Reservation> {
        val sessionFile = File(reservationsDirectory, "$sessionId.json")
        return if (sessionFile.exists()) {
            objectMapper.readValue(sessionFile)
        } else {
            // 기본 세션 데이터 반환
            val defaultFile = File(reservationsDirectory, "default_session.json")
            if (defaultFile.exists()) {
                objectMapper.readValue(defaultFile)
            } else {
                emptyList()
            }
        }
    }

    fun getAllReservations(): List<Reservation> {
        return reservationsDirectory
            .listFiles()
            ?.filter { it.extension == "json" }
            ?.flatMap { file ->
                objectMapper.readValue<List<Reservation>>(file)
            } ?: emptyList()
    }

    fun updateReservationReviewStatus(sessionId: String, productId: String, canReview: Boolean): Boolean {
        val sessionFile = File(reservationsDirectory, "$sessionId.json")
        if (!sessionFile.exists()) {
            logger.warn("세션 파일이 존재하지 않음: $sessionId")
            return false
        }

        try {
            val reservations: MutableList<Reservation> = objectMapper.readValue(sessionFile)
            var updated = false

            reservations.forEachIndexed { index, reservation ->
                // 파리 투어 상품의 경우 특별히 매칭
                if (productId == "paris-night-tour" && reservation.packageName.contains("파리", ignoreCase = true)) {
                    reservations[index] = reservation.copy(canReview = canReview)
                    updated = true
                    logger.info("예약 상태 업데이트: ${reservation.id}, canReview: $canReview")
                }
            }

            if (updated) {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(sessionFile, reservations)
                logger.info("예약 파일 업데이트 완료: $sessionId")
            }

            return updated
        } catch (e: Exception) {
            logger.error("예약 상태 업데이트 실패: $sessionId", e)
            return false
        }
    }
}