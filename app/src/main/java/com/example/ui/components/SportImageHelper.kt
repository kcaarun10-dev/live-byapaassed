package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.SportsFootball
import androidx.compose.material.icons.filled.SportsGolf
import androidx.compose.material.icons.filled.SportsHockey
import androidx.compose.material.icons.filled.SportsMotorsports
import androidx.compose.material.icons.filled.SportsMma
import androidx.compose.material.icons.filled.SportsRugby
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.SportsVolleyball
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassIceBlue
import com.example.ui.theme.GlassObsidian
import com.example.ui.theme.GlassWhiteStrong
import java.util.Locale

object SportImageHelper {

    /**
     * Resolves the primary sport icon based on category or title.
     */
    fun getSportIcon(category: String?, title: String = ""): ImageVector {
        val cat = (category ?: "").lowercase(Locale.ROOT)
        val t = title.lowercase(Locale.ROOT)

        return when {
            cat.contains("cricket") || t.contains("cricket") || t.contains("ipl") || t.contains("bbl") || t.contains("t20") || t.contains("test") -> Icons.Default.SportsCricket
            cat.contains("football") || cat.contains("soccer") || t.contains("fc") || t.contains("united") || t.contains("real madrid") || t.contains("barcelona") -> Icons.Default.SportsSoccer
            cat.contains("basketball") || cat.contains("nba") || t.contains("lakers") || t.contains("celtics") -> Icons.Default.SportsBasketball
            cat.contains("tennis") || cat.contains("atp") || cat.contains("wta") || t.contains("wimbledon") || t.contains("open") -> Icons.Default.SportsTennis
            cat.contains("motor") || cat.contains("f1") || cat.contains("formula") || cat.contains("motogp") || t.contains("grand prix") -> Icons.Default.SportsMotorsports
            cat.contains("fight") || cat.contains("combat") || cat.contains("mma") || cat.contains("ufc") || cat.contains("boxing") || cat.contains("wwe") -> Icons.Default.SportsMma
            cat.contains("american-football") || cat.contains("nfl") -> Icons.Default.SportsFootball
            cat.contains("baseball") || cat.contains("mlb") -> Icons.Default.SportsBaseball
            cat.contains("hockey") || cat.contains("nhl") -> Icons.Default.SportsHockey
            cat.contains("rugby") -> Icons.Default.SportsRugby
            cat.contains("golf") -> Icons.Default.SportsGolf
            cat.contains("volleyball") -> Icons.Default.SportsVolleyball
            else -> Icons.Default.Sports
        }
    }

    /**
     * Generates a rich, dynamic cinematic stadium gradient per sport so all cards
     * have immediate visual flair without waiting for network image requests.
     */
    fun getSportGradient(category: String?, title: String = ""): Brush {
        val cat = (category ?: "").lowercase(Locale.ROOT)
        val t = title.lowercase(Locale.ROOT)

        val colors = when {
            cat.contains("cricket") || t.contains("cricket") -> listOf(
                Color(0xFF0D1B2A),
                Color(0xFF1B263B),
                Color(0xFF0F172A)
            )
            cat.contains("football") || cat.contains("soccer") -> listOf(
                Color(0xFF1A1F2C),
                Color(0xFF242C3D),
                Color(0xFF0F172A)
            )
            cat.contains("basketball") || cat.contains("nba") -> listOf(
                Color(0xFF2A1B18),
                Color(0xFF382320),
                Color(0xFF0F172A)
            )
            cat.contains("tennis") -> listOf(
                Color(0xFF15222E),
                Color(0xFF1E3245),
                Color(0xFF0F172A)
            )
            cat.contains("motor") || cat.contains("f1") -> listOf(
                Color(0xFF2A1215),
                Color(0xFF3B1A1E),
                Color(0xFF0F172A)
            )
            cat.contains("fight") || cat.contains("mma") || cat.contains("ufc") || cat.contains("boxing") -> listOf(
                Color(0xFF24152A),
                Color(0xFF331E3C),
                Color(0xFF0F172A)
            )
            else -> listOf(
                Color(0xFF161C2A),
                Color(0xFF1F293D),
                Color(0xFF0A0E17)
            )
        }

        return Brush.linearGradient(colors)
    }
}

/**
 * Instant Sport Visual Backdrop: renders a stylized stadium insignia immediately.
 */
@Composable
fun InstantSportBackdrop(
    category: String?,
    title: String,
    modifier: Modifier = Modifier
) {
    val icon = SportImageHelper.getSportIcon(category, title)
    val gradient = SportImageHelper.getSportGradient(category, title)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        // Subtle ambient ring & sport symbol
        Surface(
            color = GlassWhiteStrong.copy(alpha = 0.35f),
            shape = CircleShape,
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderSubtle),
            modifier = Modifier.size(54.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GlassIceBlue,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
