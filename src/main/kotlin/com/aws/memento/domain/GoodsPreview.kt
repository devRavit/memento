package com.aws.memento.domain

data class GenerateGoodsPreviewRequest(
    val imageUrl: String,
    val goodsType: String,
)

data class GenerateGoodsPreviewResponse(
    val previewImageUrl: String,
)
