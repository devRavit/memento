package com.aws.memento.controller

import com.aws.memento.domain.CreateReservationRequest
import com.aws.memento.domain.Reservation
import com.aws.memento.service.ReservationStorageService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/reservations")
class ReservationController(
    private val reservationStorageService: ReservationStorageService,
) {
    @PostMapping
    fun createReservation(
        @RequestBody request: CreateReservationRequest,
    ): ResponseEntity<Reservation> {
        val reservation = reservationStorageService.createReservation(request)
        return ResponseEntity.ok(reservation)
    }

    @GetMapping("/session")
    fun getReservationsBySessionId(
        @RequestParam sessionId: String,
    ): ResponseEntity<List<Reservation>> {
        val reservations = reservationStorageService.getReservationsBySessionId(sessionId)
        return ResponseEntity.ok(reservations)
    }

    @GetMapping
    fun getAllReservations(): ResponseEntity<List<Reservation>> {
        val reservations = reservationStorageService.getAllReservations()
        return ResponseEntity.ok(reservations)
    }
}