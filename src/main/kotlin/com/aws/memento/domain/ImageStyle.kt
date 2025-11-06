package com.aws.memento.domain

enum class ImageStyle(
    val displayName: String,
    val description: String,
    val category: StyleCategory,
) {
    REALISTIC("사실적", "Photorealistic style with high detail and natural lighting", StyleCategory.STYLE_TRANSFORM),
    ANIME("애니메이션", "Anime or manga style with vibrant colors and expressive features", StyleCategory.STYLE_TRANSFORM),
    WATERCOLOR("수채화", "Soft watercolor painting style with flowing colors and gentle textures", StyleCategory.STYLE_TRANSFORM),
    OIL_PAINTING("유화", "Classic oil painting style with rich textures and bold brushstrokes", StyleCategory.STYLE_TRANSFORM),
    SKETCH("스케치", "Hand-drawn sketch style with pencil or charcoal textures", StyleCategory.STYLE_TRANSFORM),
    CYBERPUNK("사이버펑크", "Futuristic cyberpunk style with neon lights and dystopian themes", StyleCategory.STYLE_TRANSFORM),
    VINTAGE("빈티지", "Retro vintage style with aged colors and nostalgic atmosphere", StyleCategory.STYLE_TRANSFORM),
    MINIMALIST("미니멀", "Clean minimalist style with simple shapes and limited colors", StyleCategory.STYLE_TRANSFORM),
    FANTASY("판타지", "Magical fantasy style with ethereal and dreamlike qualities", StyleCategory.STYLE_TRANSFORM),
    POP_ART("팝아트", "Bold pop art style with vibrant colors and graphic elements", StyleCategory.STYLE_TRANSFORM),

    BRIGHT("밝게", "Enhance brightness and exposure for better visibility in dark photos", StyleCategory.PHOTO_ENHANCEMENT),
    VIVID("선명하게", "Enhance color vibrancy and sharpness for more impactful photos", StyleCategory.PHOTO_ENHANCEMENT),
    CLARITY("또렷하게", "Improve sharpness and reduce blur for clearer details", StyleCategory.PHOTO_ENHANCEMENT),
    PROFESSIONAL("전문가 보정", "Comprehensive professional photo enhancement with optimal brightness, color, and clarity", StyleCategory.PHOTO_ENHANCEMENT),

    BLEMISH_REMOVAL("잡티 제거", "Remove skin blemishes, spots, and imperfections for smoother complexion", StyleCategory.PHOTO_ENHANCEMENT),
    SKIN_SMOOTHING("피부 보정", "Smooth skin texture while maintaining natural appearance and details", StyleCategory.PHOTO_ENHANCEMENT),
    FACE_ENHANCE("얼굴 보정", "Enhance facial features with natural beauty adjustments", StyleCategory.PHOTO_ENHANCEMENT),
    WHITENING("미백", "Brighten skin tone naturally while preserving realistic appearance", StyleCategory.PHOTO_ENHANCEMENT),
    BACKGROUND_PEOPLE_REMOVAL("배경 인물 제거", "Remove people in the background while keeping main subjects intact", StyleCategory.PHOTO_ENHANCEMENT),
}

enum class StyleCategory {
    STYLE_TRANSFORM,
    PHOTO_ENHANCEMENT,
}
