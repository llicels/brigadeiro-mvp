package com.brigadeiro.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.brigadeiro.app.ui.theme.MossContainer
import com.brigadeiro.app.ui.theme.NeutralField

/**
 * Delivery-style image, "photo before text". Shows the remote [imageUrl] when
 * present; while loading, on failure, or when the app is offline it falls back
 * to a branded gradient tile with the dish emoji, so the layout never breaks.
 */
@Composable
fun RecipeImage(
    slug: String,
    emoji: String,
    imageUrl: String? = null,
    modifier: Modifier = Modifier,
    height: Dp = 160.dp,
    emojiSize: TextUnit = 58.sp,
    scrimBottom: Boolean = false,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUrl.isNullOrBlank()) {
            IllustratedTile(slug = slug, emoji = emoji, emojiSize = emojiSize)
        } else {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { IllustratedTile(slug = slug, emoji = emoji, emojiSize = emojiSize) },
                error = { IllustratedTile(slug = slug, emoji = emoji, emojiSize = emojiSize) },
            )
        }

        if (scrimBottom) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.45f to Color.Transparent,
                            1f to Color(0xCC000000),
                        ),
                    ),
            )
        }
    }
}

@Composable
private fun IllustratedTile(
    slug: String,
    emoji: String,
    emojiSize: TextUnit,
) {
    val context = LocalContext.current
    val resourceId = remember(slug) {
        context.resources.getIdentifier("recipe_$slug", "drawable", context.packageName)
    }
    if (resourceId != 0) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(resourceId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        return
    }
    val colors = remember(slug) { gradientFor(slug) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(colors)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, fontSize = emojiSize)
    }
}

private fun gradientFor(slug: String): List<Color> {
    val palettes = listOf(
        listOf(MossContainer, NeutralField),
        listOf(Color(0xFFE9F0E1), MossContainer),
        listOf(Color(0xFFDDECD0), NeutralField),
        listOf(MossContainer, Color(0xFFF0F4EC)),
    )
    val index = ((slug.hashCode() % palettes.size) + palettes.size) % palettes.size
    return palettes[index]
}
