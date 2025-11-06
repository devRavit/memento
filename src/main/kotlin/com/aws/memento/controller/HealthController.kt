package com.aws.memento.controller

import com.aws.memento.controller.dto.HealthResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1")
class HealthController {
    @GetMapping("/health")
    fun health(): ResponseEntity<HealthResponse> {
        return ResponseEntity.ok(
            HealthResponse(
                status = "UP",
                timestamp = LocalDateTime.now().toString(),
            ),
        )
    }

    @GetMapping
    fun root(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(
            mapOf(
                "service" to "Memento API",
                "version" to "1.0.0",
                "status" to "running",
            ),
        )
    }
}
