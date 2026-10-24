package com.brigadeiro.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.brigadeiro.app.ui.theme.Moss
import com.brigadeiro.app.ui.theme.MossDark
import com.brigadeiro.app.ui.theme.Spacing

/**
 * Promotional banner: bold gradient card with a headline and a supporting line.
 * Purely presentational (no fake CTAs).
 */
@Composable
fun PromoBanner(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(20.dp)
    val brush = Brush.linearGradient(listOf(Moss, MossDark))
    if (onClick != null) {
        Surface(
            onClick = onClick,
            shape = shape,
            color = Moss,
            modifier = modifier.fillMaxWidth(),
        ) {
            BannerContent(title, subtitle, icon, brush)
        }
    } else {
        Surface(
            shape = shape,
            color = Moss,
            modifier = modifier.fillMaxWidth(),
        ) {
            BannerContent(title, subtitle, icon, brush)
        }
    }
}

@Composable
private fun BannerContent(
    title: String,
    subtitle: String,
    icon: ImageVector,
    brush: Brush,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush)
            .height(124.dp)
            .padding(Spacing.lg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 72.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
            )
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(64.dp),
        )
    }
}
