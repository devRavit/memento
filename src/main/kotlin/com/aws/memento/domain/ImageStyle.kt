package com.aws.memento.domain

enum class ImageStyle(
    val displayName: String,
    val description: String,
) {
    REALISTIC("사실적", "Photorealistic style with high detail and natural lighting"),
    ANIME("애니메이션", "Anime or manga style with vibrant colors and expressive features"),
    WATERCOLOR("수채화", "Soft watercolor painting style with flowing colors and gentle textures"),
    OIL_PAINTING("유화", "Classic oil painting style with rich textures and bold brushstrokes"),
    SKETCH("스케치", "Hand-drawn sketch style with pencil or charcoal textures"),
    CYBERPUNK("사이버펑크", "Futuristic cyberpunk style with neon lights and dystopian themes"),
    VINTAGE("빈티지", "Retro vintage style with aged colors and nostalgic atmosphere"),
    MINIMALIST("미니멀", "Clean minimalist style with simple shapes and limited colors"),
    FANTASY("판타지", "Magical fantasy style with ethereal and dreamlike qualities"),
    POP_ART("팝아트", "Bold pop art style with vibrant colors and graphic elements"),
}
