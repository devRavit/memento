package com.aws.memento.controller

import com.aws.memento.domain.GenerateGoodsPreviewRequest
import com.aws.memento.domain.GenerateGoodsPreviewResponse
import com.aws.memento.service.GoodsPreviewService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/images")
class ImageController(
    private val goodsPreviewService: GoodsPreviewService,
) {
    @PostMapping("/generate-goods-preview")
    fun generateGoodsPreview(
        @RequestBody request: GenerateGoodsPreviewRequest,
    ): ResponseEntity<GenerateGoodsPreviewResponse> {
        val previewImageUrl = goodsPreviewService.generateGoodsPreview(request.imageUrl, request.goodsType)
        return ResponseEntity.ok(GenerateGoodsPreviewResponse(previewImageUrl))
    }
}
