package com.aws.memento.config

import com.aws.memento.websocket.ChatWebSocketHandler
import com.aws.memento.websocket.ImageGenerationWebSocketHandler
import com.aws.memento.websocket.ProgressWebSocketHandler
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val progressWebSocketHandler: ProgressWebSocketHandler,
    private val chatWebSocketHandler: ChatWebSocketHandler,
    private val imageGenerationWebSocketHandler: ImageGenerationWebSocketHandler,
) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(progressWebSocketHandler, "/ws/progress")
            .setAllowedOrigins("http://localhost:9999")
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
            .setAllowedOrigins("http://localhost:9999")
        registry.addHandler(imageGenerationWebSocketHandler, "/ws/image-generation")
            .setAllowedOrigins("http://localhost:9999")
    }
}
