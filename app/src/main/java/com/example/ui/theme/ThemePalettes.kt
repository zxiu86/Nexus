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

    // Multi-Color Card Gradient Themes (تدرجات متعددة متناسقة مخصصة لأنيميشن بطاقات أحدث الأعمال فقط - 2 إلى 3 ألوان)
    val GRADIENT_PRESETS = listOf(
        ThemePalettePreset(
            id = 10,
            name = "تدرج ذهبي دافئ",
            categoryName = "تدرجات أحدث الأعمال",
            isMultiColor = true,
            primaryColor = NexusGold,
            secondaryColor = NexusOrange,
            gradientColors = listOf(NexusGold, NexusOrange, NexusGoldLight),
            onPrimary = BackgroundDark,
            badgeLabel = "تدرج للبطاقات"
        ),
        ThemePalettePreset(
            id = 11,
            name = "تدرج أزرق ملكي",
            categoryName = "تدرجات أحدث الأعمال",
            isMultiColor = true,
            primaryColor = NexusBluePrimary,
            secondaryColor = NexusBlueDark,
            gradientColors = listOf(NexusBluePrimary, NexusBlueLight, NexusBlueDark),
            onPrimary = Color.White,
            badgeLabel = "تدرج للبطاقات"
        ),
        ThemePalettePreset(
            id = 12,
            name = "تدرج أحمر قرمزي",
            categoryName = "تدرجات أحدث الأعمال",
            isMultiColor = true,
            primaryColor = NexusRedPrimary,
            secondaryColor = NexusRedDark,
            gradientColors = listOf(NexusRedPrimary, NexusRedLight, NexusRedDark),
            onPrimary = Color.White,
            badgeLabel = "تدرج للبطاقات"
        ),
        ThemePalettePreset(
            id = 13,
            name = "تدرج أزرق بحري",
            categoryName = "تدرجات أحدث الأعمال",
            isMultiColor = true,
            primaryColor = NexusMarineBluePrimary,
            secondaryColor = NexusMarineBlueLight,
            gradientColors = listOf(NexusMarineBluePrimary, NexusMarineBlueLight, NexusMarineBlueDark),
            onPrimary = Color.White,
            badgeLabel = "تدرج للبطاقات"
        ),
        ThemePalettePreset(
            id = 14,
            name = "تدرج أزهار الكرز",
            categoryName = "تدرجات أحدث الأعمال",
            isMultiColor = true,
            primaryColor = NexusCherryBlossomPrimary,
            secondaryColor = NexusCherryBlossomLight,
            gradientColors = listOf(NexusCherryBlossomPrimary, NexusCherryBlossomLight, NexusCherryBlossomDark),
            onPrimary = Color.Black,
            badgeLabel = "تدرج للبطاقات"
        ),
        ThemePalettePreset(
            id = 15,
            name = "تدرج زمردي نقي",
            categoryName = "تدرجات أحدث الأعمال",
            isMultiColor = true,
            primaryColor = NexusEmeraldPrimary,
            secondaryColor = NexusEmeraldDark,
            gradientColors = listOf(NexusEmeraldPrimary, NexusEmeraldLight, NexusEmeraldDark),
            onPrimary = Color.White,
            badgeLabel = "تدرج للبطاقات"
        ),
        ThemePalettePreset(
            id = 16,
            name = "تدرج بنفسجي ملكي",
            categoryName = "تدرجات أحدث الأعمال",
            isMultiColor = true,
            primaryColor = NexusVioletPrimary,
            secondaryColor = NexusVioletDark,
            gradientColors = listOf(NexusVioletPrimary, NexusVioletLight, NexusVioletDark),
            onPrimary = Color.White,
            badgeLabel = "تدرج للبطاقات"
        ),
        ThemePalettePreset(
            id = 17,
            name = "تدرج كهرماني مشرق",
            categoryName = "تدرجات أحدث الأعمال",
            isMultiColor = true,
            primaryColor = NexusAmberPrimary,
            secondaryColor = NexusAmberDark,
            gradientColors = listOf(NexusAmberPrimary, NexusAmberLight, NexusAmberDark),
            onPrimary = Color.Black,
            badgeLabel = "تدرج للبطاقات"
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
