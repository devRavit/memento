package com.aws.memento.controller.dto

import com.aws.memento.domain.ImageStyle

data class ImageGenerationRequest(
    val imageFileNames: List<String>,
    val style: ImageStyle,
    val additionalPrompt: String? = null,
)
