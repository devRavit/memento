package com.aws.memento.controller

import com.aws.memento.service.ProgressSimulationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/test")
class WebSocketTestController(
    private val progressSimulationService: ProgressSimulationService,
) {
    data class StartProcessRequest(
        val sessionId: String,
    )

    @PostMapping("/start-process")
    fun startProcess(
        @RequestBody request: StartProcessRequest,
    ): ResponseEntity<Map<String, Any>> {
        progressSimulationService.startSimulation(request.sessionId)

        return ResponseEntity.ok(
            mapOf(
                "status" to "STARTED",
                "sessionId" to request.sessionId,
                "message" to "Processing started",
            ),
        )
    }
}
