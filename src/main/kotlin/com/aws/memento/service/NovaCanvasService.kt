package com.aws.memento.service

import org.springframework.stereotype.Service

interface NovaCanvasService {
    fun generateImage(prompt: String): String
}

@Service
class NovaCanvasServiceImpl : NovaCanvasService {
    override fun generateImage(prompt: String): String {
        println("Nova Canvas Prompt: $prompt")

        return "https://memento-bucket.s3.amazonaws.com/generated/image_${System.currentTimeMillis()}.png"
    }
}
