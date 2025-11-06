package com.aws.memento.controller

import com.aws.memento.controller.dto.CreateSouvenirRequest
import com.aws.memento.controller.dto.SouvenirResponse
import com.aws.memento.controller.dto.toResponse
import com.aws.memento.service.SouvenirService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/souvenirs")
class SouvenirController(
    private val souvenirService: SouvenirService,
) {
    @PostMapping
    fun createSouvenir(
        @RequestBody request: CreateSouvenirRequest,
    ): ResponseEntity<SouvenirResponse> {
        val souvenir = souvenirService.createSouvenir(request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(souvenir.toResponse())
    }

    @GetMapping("/{id}")
    fun getSouvenir(
        @PathVariable id: String,
    ): ResponseEntity<SouvenirResponse> {
        val souvenir =
            souvenirService.getSouvenir(id)
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(souvenir.toResponse())
    }

    @GetMapping
    fun listSouvenirs(
        @RequestParam userId: String,
    ): ResponseEntity<List<SouvenirResponse>> {
        val souvenirs = souvenirService.listSouvenirs(userId)
        return ResponseEntity.ok(souvenirs.map { it.toResponse() })
    }
}
