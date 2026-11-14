package com.botillon.blocksmith.ui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.botillon.blocksmith.game.Palette

/**
 * Direction artistique : nuit indigo, blocs « résine » brillants, accent ambre.
 * Les couleurs de pièces sont volontairement distinctes de la référence.
 */
object Ink {
    val BgTop = Color(0xFF100D1B)
    val BgMid = Color(0xFF191331)
    val BgBottom = Color(0xFF0D0B16)

    val Surface = Color(0xFF221C3B)
    val SurfaceHigh = Color(0xFF2C2550)
    val SurfaceLow = Color(0xFF1A1530)

    val BoardFrame = Color(0xFF1B1630)
    val CellEmpty = Color(0xFF2A2445)

    val Text = Color(0xFFF7F4FF)
    val TextSoft = Color(0xFFCBC2E8)
    val Muted = Color(0xFF8F86B0)
    val Faint = Color(0xFF4B4368)

    val Accent = Color(0xFFFFC857)
    val AccentDeep = Color(0xFFF09B3E)
    val Combo = Color(0xFFB892FF)
    val Mint = Color(0xFF3DDC97)
    val Danger = Color(0xFFFF6B81)

    val Shadow = Color(0xFF05040A)
}

/** Dégradé et teintes d'une couleur de pièce. */
data class BlockStyle(
    val base: Color,
    val light: Color,
    val dark: Color,
    val glow: Color,
)

private val styles = mapOf(
    Palette.CORAL to BlockStyle(Color(0xFFFF6B6B), Color(0xFFFF9A8F), Color(0xFFD94A5C), Color(0xFFFF6B6B)),
    Palette.AMBER to BlockStyle(Color(0xFFFFC857), Color(0xFFFFE29A), Color(0xFFE09E2C), Color(0xFFFFC857)),
    Palette.MINT to BlockStyle(Color(0xFF3DDC97), Color(0xFF86F3C4), Color(0xFF1FAE74), Color(0xFF3DDC97)),
    Palette.SKY to BlockStyle(Color(0xFF4CC9F0), Color(0xFF9BE8FF), Color(0xFF219BC4), Color(0xFF4CC9F0)),
    Palette.VIOLET to BlockStyle(Color(0xFFA78BFA), Color(0xFFD0C0FF), Color(0xFF7C5FE0), Color(0xFFA78BFA)),
    Palette.ROSE to BlockStyle(Color(0xFFF472B6), Color(0xFFFFA9D5), Color(0xFFD14E8F), Color(0xFFF472B6)),
)

fun blockStyle(palette: Palette): BlockStyle = styles.getValue(palette)

/**
 * Échelle typographique. La famille est injectée une fois au démarrage depuis
 * [App] : les polices embarquées sont chargées de façon asynchrone par Compose
 * Resources, et tous les styles partagent la même famille.
 */
object Typo {
    var family: FontFamily = FontFamily.Default

    val display: TextStyle
        get() = TextStyle(
            fontSize = 46.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-1.2).sp,
            fontFamily = family,
        )

    val score: TextStyle
        get() = TextStyle(
            fontSize = 54.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-2).sp,
            fontFamily = family,
        )

    val title: TextStyle
        get() = TextStyle(
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.4).sp,
            fontFamily = family,
        )

    val heading: TextStyle
        get() = TextStyle(
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.2).sp,
            fontFamily = family,
        )

    val body: TextStyle
        get() = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = family,
        )

    val label: TextStyle
        get() = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp,
            fontFamily = family,
        )

    val button: TextStyle
        get() = TextStyle(
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.6.sp,
            fontFamily = family,
        )
}

/** Rayon des coins d'un bloc, proportionnel à sa taille. */
fun blockCorner(size: Float): Float = size * 0.24f

/**
 * Dessine un bloc « résine » : socle coloré, dégradé vertical, reflet supérieur,
 * ombre interne en bas et liseré clair. [size] est le côté de la case occupée.
 */
fun DrawScope.drawBlock(
    topLeft: Offset,
    size: Float,
    style: BlockStyle,
    scale: Float = 1f,
    alpha: Float = 1f,
    gem: Boolean = false,
) {
    if (size <= 0f || alpha <= 0f) return
    val inset = size * 0.045f
    val s = (size - inset * 2) * scale
    val corner = blockCorner(s)
    val o = Offset(
        topLeft.x + (size - s) / 2f,
        topLeft.y + (size - s) / 2f,
    )
    val radius = CornerRadius(corner, corner)

    // Ombre portée douce.
    drawRoundRect(
        color = Ink.Shadow.copy(alpha = 0.45f * alpha),
        topLeft = Offset(o.x, o.y + s * 0.07f),
        size = Size(s, s),
        cornerRadius = radius,
    )

    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(style.light, style.base),
            startY = o.y,
            endY = o.y + s,
        ),
        topLeft = o,
        size = Size(s, s),
        cornerRadius = radius,
        alpha = alpha,
    )

    // Reflet supérieur.
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.White.copy(alpha = 0.30f * alpha), Color.White.copy(alpha = 0f)),
            startY = o.y,
            endY = o.y + s * 0.55f,
        ),
        topLeft = Offset(o.x + s * 0.10f, o.y + s * 0.07f),
        size = Size(s * 0.80f, s * 0.42f),
        cornerRadius = CornerRadius(corner * 0.7f, corner * 0.7f),
    )

    // Ombre interne en bas.
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, style.dark.copy(alpha = 0.55f * alpha)),
            startY = o.y + s * 0.62f,
            endY = o.y + s,
        ),
        topLeft = o,
        size = Size(s, s),
        cornerRadius = radius,
    )

    // Liseré.
    drawRoundRect(
        color = Color.White.copy(alpha = 0.16f * alpha),
        topLeft = o,
        size = Size(s, s),
        cornerRadius = radius,
        style = Stroke(width = (s * 0.022f).coerceAtLeast(0.8f)),
    )

    if (gem) drawGem(Offset(o.x + s / 2f, o.y + s / 2f), s * 0.30f, alpha)
}

/** Petite gemme en losange, avec halo. */
fun DrawScope.drawGem(center: Offset, radius: Float, alpha: Float = 1f) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.55f * alpha), Color.Transparent),
            center = center,
            radius = radius * 2.6f,
        ),
        radius = radius * 2.6f,
        center = center,
    )
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(center.x, center.y - radius)
        lineTo(center.x + radius * 0.72f, center.y)
        lineTo(center.x, center.y + radius)
        lineTo(center.x - radius * 0.72f, center.y)
        close()
    }
    drawPath(path, Color.White.copy(alpha = 0.92f * alpha))
    drawPath(path, Color.White.copy(alpha = 0.35f * alpha), style = Stroke(width = radius * 0.18f))
}

/** Petite case vide du plateau. */
fun DrawScope.drawEmptyCell(topLeft: Offset, size: Float, alpha: Float = 1f) {
    val inset = size * 0.045f
    val s = size - inset * 2
    drawRoundRect(
        color = Ink.CellEmpty.copy(alpha = alpha),
        topLeft = Offset(topLeft.x + inset, topLeft.y + inset),
        size = Size(s, s),
        cornerRadius = CornerRadius(blockCorner(s), blockCorner(s)),
    )
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.White.copy(alpha = 0.035f * alpha), Color.Transparent),
            startY = topLeft.y,
            endY = topLeft.y + size * 0.6f,
        ),
        topLeft = Offset(topLeft.x + inset, topLeft.y + inset),
        size = Size(s, s),
        cornerRadius = CornerRadius(blockCorner(s), blockCorner(s)),
    )
}

fun TextUnit.scaled(factor: Float): TextUnit = (value * factor).sp
