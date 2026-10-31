@file:Suppress("FunctionName")

package com.nikitaberezhnyj.saycheese.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nikitaberezhnyj.saycheese.R
import com.nikitaberezhnyj.saycheese.components.PrimaryButton
import com.nikitaberezhnyj.saycheese.components.ScreenLayout
import com.nikitaberezhnyj.saycheese.theme.*

@Composable
fun HowItWorksScreen(
    onContinue: () -> Unit
) {
    ScreenLayout(
        icon = {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(SurfaceDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Mic,
                    contentDescription = null,
                    tint = Accent,
                    modifier = Modifier.size(IconSize.Large)
                )
            }
        },
        title = stringResource(R.string.how_it_works_title),
        description = stringResource(R.string.how_it_works_subtitle)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.M)
        ) {
            InstructionItem(
                icon = Icons.Outlined.Mic,
                title = stringResource(R.string.how_it_works_cheese_title),
                description = stringResource(R.string.how_it_works_cheese_desc)
            )

            InstructionItem(
                icon = Icons.Outlined.Timer,
                title = stringResource(R.string.how_it_works_timer_title),
                description = stringResource(R.string.how_it_works_timer_desc)
            )
        }

        Spacer(Modifier.height(Spacing.XL))

        PrimaryButton(
            text = stringResource(R.string.how_it_works_continue),
            onClick = onContinue
        )
    }
}

@Composable
private fun InstructionItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = SurfaceDark, shape = RoundedCornerShape(Radius.M))
            .padding(Spacing.M),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(BackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(IconSize.Small)
            )
        }

        Spacer(Modifier.width(Spacing.M))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = FontSize.Body,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(Modifier.height(Spacing.XS))
            Text(
                text = description,
                fontSize = FontSize.Small,
                color = TextSecondary,
                lineHeight = FontSize.Body
            )
        }
    }
}