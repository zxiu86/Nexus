package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Data structure representing a Theme & Accent Palette (Solid or Multi-Color Gradient).
 */
data class ThemePalettePreset(
    val id: Int,
    val name: String,
    val categoryName: String,
    val isMultiColor: Boolean,
    val primaryColor: Color,
    val secondaryColor: Color,
    val gradientColors: List<Color>,
    val onPrimary: Color = Color.White,
    val badgeLabel: String = if (isMultiColor) "تدرج جمالي" else "أساسي"
) {
    val brush: Brush
        get() = if (gradientColors.size > 1) {
            Brush.linearGradient(gradientColors)
        } else {
            Brush.linearGradient(listOf(primaryColor, secondaryColor))
        }
}

object ThemePalettes {

    // Solid Primary Themes (الألوان الأساسية)
    val SOLID_PRESETS = listOf(
        ThemePalettePreset(
            id = 0,
            name = "ذهبي دافئ",
            categoryName = "الأساسية",
            isMultiColor = false,
            primaryColor = NexusGold,
            secondaryColor = NexusOrange,
            gradientColors = listOf(NexusGold, NexusOrange),
            onPrimary = BackgroundDark
        ),
        ThemePalettePreset(
            id = 1,
            name = "أزرق ملكي",
            categoryName = "الأساسية",
            isMultiColor = false,
            primaryColor = NexusBluePrimary,
            secondaryColor = NexusBlueDark,
            gradientColors = listOf(NexusBluePrimary, NexusBlueDark),
            onPrimary = Color.White
        ),
        ThemePalettePreset(
            id = 2,
            name = "أحمر قرمزي",
            categoryName = "الأساسية",
            isMultiColor = false,
            primaryColor = NexusRedPrimary,
            secondaryColor = NexusRedDark,
            gradientColors = listOf(NexusRedPrimary, NexusRedDark),
            onPrimary = Color.White
        ),
        ThemePalettePreset(
            id = 3,
            name = "أزرق بحري",
            categoryName = "الأساسية",
            isMultiColor = false,
            primaryColor = NexusMarineBluePrimary,
            secondaryColor = NexusMarineBlueLight,
            gradientColors = listOf(NexusMarineBluePrimary, NexusMarineBlueLight),
            onPrimary = Color.White
        ),
        ThemePalettePreset(
            id = 4,
            name = "أزهار الكرز",
            categoryName = "الأساسية",
            isMultiColor = false,
            primaryColor = NexusCherryBlossomPrimary,
            secondaryColor = NexusCherryBlossomLight,
            gradientColors = listOf(NexusCherryBlossomPrimary, NexusCherryBlossomLight),
            onPrimary = Color.Black
        ),
        ThemePalettePreset(
            id = 5,
            name = "زمردي نقي",
            categoryName = "الأساسية",
            isMultiColor = false,
            primaryColor = NexusEmeraldPrimary,
            secondaryColor = NexusEmeraldDark,
            gradientColors = listOf(NexusEmeraldPrimary, NexusEmeraldDark),
            onPrimary = Color.White
        ),
        ThemePalettePreset(
            id = 6,
            name = "بنفسجي ملكي",
            categoryName = "الأساسية",
            isMultiColor = false,
            primaryColor = NexusVioletPrimary,
            secondaryColor = NexusVioletDark,
            gradientColors = listOf(NexusVioletPrimary, NexusVioletDark),
            onPrimary = Color.White
        ),
        ThemePalettePreset(
            id = 7,
            name = "كهرماني مشرق",
            categoryName = "الأساسية",
            isMultiColor = false,
            primaryColor = NexusAmberPrimary,
            secondaryColor = NexusAmberDark,
            gradientColors = listOf(NexusAmberPrimary, NexusAmberDark),
            onPrimary = Color.Black
        )
    )

    // Multi-Color Aesthetic Gradient Themes (تدرجات ألوان تجميلية متعددة)
    val GRADIENT_PRESETS = listOf(
        ThemePalettePreset(
            id = 10,
            name = "الشفق القطبي (Aurora)",
            categoryName = "تدرجات تجميلية",
            isMultiColor = true,
            primaryColor = AuroraCyan,
            secondaryColor = AuroraViolet,
            gradientColors = listOf(AuroraCyan, AuroraViolet, AuroraEmerald),
            onPrimary = Color.Black
        ),
        ThemePalettePreset(
            id = 11,
            name = "غروب الشمس (Sunset)",
            categoryName = "تدرجات تجميلية",
            isMultiColor = true,
            primaryColor = SunsetOrange,
            secondaryColor = SunsetPink,
            gradientColors = listOf(SunsetOrange, SunsetPink, SunsetGold),
            onPrimary = Color.White
        ),
        ThemePalettePreset(
            id = 12,
            name = "نيون سايبربانك (Cyberpunk)",
            categoryName = "تدرجات تجميلية",
            isMultiColor = true,
            primaryColor = CyberpunkPink,
            secondaryColor = CyberpunkCyan,
            gradientColors = listOf(CyberpunkPink, CyberpunkPurple, CyberpunkCyan),
            onPrimary = Color.White
        ),
        ThemePalettePreset(
            id = 13,
            name = "أمواج المحيط (Ocean Wave)",
            categoryName = "تدرجات تجميلية",
            isMultiColor = true,
            primaryColor = OceanSky,
            secondaryColor = OceanTeal,
            gradientColors = listOf(OceanSky, OceanTeal, OceanBlue),
            onPrimary = Color.White
        ),
        ThemePalettePreset(
            id = 14,
            name = "لهب التنين (Dragon Fire)",
            categoryName = "تدرجات تجميلية",
            isMultiColor = true,
            primaryColor = DragonRed,
            secondaryColor = DragonGold,
            gradientColors = listOf(DragonRed, DragonOrange, DragonGold),
            onPrimary = Color.White
        ),
        ThemePalettePreset(
            id = 15,
            name = "سديم الفضاء (Cosmic Nebula)",
            categoryName = "تدرجات تجميلية",
            isMultiColor = true,
            primaryColor = NebulaIndigo,
            secondaryColor = NebulaFuchsia,
            gradientColors = listOf(NebulaIndigo, NebulaFuchsia, NebulaStarlight),
            onPrimary = Color.White
        )
    )

    val ALL_PRESETS: List<ThemePalettePreset> = SOLID_PRESETS + GRADIENT_PRESETS

    fun getPresetById(id: Int): ThemePalettePreset {
        return ALL_PRESETS.firstOrNull { it.id == id } ?: SOLID_PRESETS.first()
    }

    fun isMultiColorTheme(id: Int): Boolean {
        return getPresetById(id).isMultiColor
    }

    fun getGradientColors(id: Int): List<Color> {
        return getPresetById(id).gradientColors
    }
}
