package com.example.stismartlib.ui.components
import com.composables.icons.lucide.*

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.ui.theme.AccentYellow
import com.example.stismartlib.ui.theme.AccentYellow35
import com.example.stismartlib.ui.theme.BrandBorder
import com.example.stismartlib.ui.theme.BrandBorderSoft
import com.example.stismartlib.ui.theme.BrandBorderStrong
import com.example.stismartlib.ui.theme.BrandPlaceholder
import com.example.stismartlib.ui.theme.CardShadow
import com.example.stismartlib.ui.theme.EyebrowStyle
import com.example.stismartlib.ui.theme.PrimaryBlue
import com.example.stismartlib.ui.theme.PrimaryBlue5
import com.example.stismartlib.ui.theme.PrimaryBlue10

/** Web: rounded-xl = 12dp, rounded-2xl = 16dp, rounded-3xl = 24dp */
val ShapeXl = RoundedCornerShape(12.dp)
val Shape2Xl = RoundedCornerShape(16.dp)
val Shape3Xl = RoundedCornerShape(24.dp)
val ShapePill = RoundedCornerShape(999.dp)

@Composable
fun LibraryMarkTile(
    size: Dp = 40.dp,
    contentDescription: String? = null
) {
    Box(
        modifier = Modifier
            .size(size)
            .shadow(1.dp, ShapeXl, spotColor = CardShadow, ambientColor = CardShadow)
            .clip(ShapeXl)
            .background(AccentYellow),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Lucide.LibraryBig,
            contentDescription = contentDescription,
            tint = PrimaryBlue,
            modifier = Modifier.size(size * 0.55f)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(4.dp)
                .background(PrimaryBlue)
        )
    }
}

@Composable
fun LibraryMark(onDark: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        LibraryMarkTile()
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "STI ORMOC",
                color = if (onDark) Color.White else PrimaryBlue,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                letterSpacing = (-0.01).sp
            )
            Text(
                text = "SMART LIBRARY",
                color = if (onDark) Color.White.copy(alpha = 0.7f) else PrimaryBlue.copy(alpha = 0.6f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                letterSpacing = (0.16 * 10).sp
            )
        }
    }
}

@Composable
fun PageHeader(
    eyebrow: String,
    title: String,
    description: String? = null,
    action: @Composable (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = eyebrow.uppercase(),
            style = EyebrowStyle,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (description != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                lineHeight = 20.sp
            )
        }
        if (action != null) {
            Spacer(modifier = Modifier.height(16.dp))
            action()
        }
    }
}

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(1.dp, Shape2Xl, spotColor = CardShadow, ambientColor = CardShadow)
            .clip(Shape2Xl)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, Shape2Xl)
            .padding(contentPadding)
    ) {
        content()
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    note: String? = null,
    icon: ImageVector,
    highlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    SectionCard(modifier = modifier, contentPadding = 16.dp) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (note != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        note,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        fontSize = 11.sp
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(ShapeXl)
                    .background(if (highlight) AccentYellow35 else PrimaryBlue5)
                    .padding(10.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (highlight) PrimaryBlue else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun StatusBadge(label: String, warning: Boolean = false) {
    val background = if (warning) AccentYellow35 else PrimaryBlue5
    Text(
        text = label,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(ShapePill)
            .background(background)
            .border(1.dp, BrandBorderSoft, ShapePill)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

enum class PortalButtonVariant { Primary, Secondary, Ghost, Yellow }

@Composable
fun PortalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: PortalButtonVariant = PortalButtonVariant.Primary,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    val background = when (variant) {
        PortalButtonVariant.Primary -> PrimaryBlue
        PortalButtonVariant.Secondary -> MaterialTheme.colorScheme.surface
        PortalButtonVariant.Ghost -> Color.Transparent
        PortalButtonVariant.Yellow -> AccentYellow
    }
    val contentColor = when (variant) {
        PortalButtonVariant.Primary -> Color.White
        PortalButtonVariant.Yellow -> PrimaryBlue
        else -> MaterialTheme.colorScheme.onSurface
    }
    val borderColor = when (variant) {
        PortalButtonVariant.Secondary -> BrandBorder
        else -> Color.Transparent
    }
    val showShadow = variant == PortalButtonVariant.Primary

    Row(
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .then(
                if (showShadow) Modifier.shadow(2.dp, ShapeXl, spotColor = PrimaryBlue10, ambientColor = PrimaryBlue10)
                else Modifier
            )
            .clip(ShapeXl)
            .background(if (enabled) background else background.copy(alpha = 0.45f))
            .border(1.dp, borderColor, ShapeXl)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, tint = contentColor, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text, color = contentColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
fun PortalField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: @Composable (() -> Unit)? = null,
    error: String? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val borderColor = when {
        error != null -> PrimaryBlue
        focused -> PrimaryBlue
        else -> BrandBorderStrong
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (label.isNotEmpty()) {
            Text(
                label,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 52.dp)
                .clip(ShapeXl)
                .border(1.dp, borderColor, ShapeXl)
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        placeholder,
                        color = BrandPlaceholder,
                        fontSize = 14.sp
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    interactionSource = interaction,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                    visualTransformation = visualTransformation,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (trailing != null) {
                Box(
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    trailing()
                }
            }
        }
        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                error,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
