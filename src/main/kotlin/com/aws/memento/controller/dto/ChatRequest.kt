package com.aws.memento.controller.dto

data class ChatRequest(
    val message: String,
    val conversationHistory: List<ChatMessage> = emptyList(),
)

data class ChatMessage(
    val role: String,
    val content: String,
)
