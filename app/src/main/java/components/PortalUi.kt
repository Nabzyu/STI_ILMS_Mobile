package com.example.stismartlib.ui.components

import com.composables.icons.lucide.*

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.ui.theme.AccentYellow
import com.example.stismartlib.R
import com.example.stismartlib.ui.theme.AccentYellow35
import com.example.stismartlib.ui.theme.CardShadow
import com.example.stismartlib.ui.theme.ElmsCyan
import com.example.stismartlib.ui.theme.ElmsCyanSoft
import com.example.stismartlib.ui.theme.EyebrowStyle
import com.example.stismartlib.ui.theme.StiMarkBlue
import com.example.stismartlib.ui.theme.StiYellow

val ShapeXl = RoundedCornerShape(12.dp)
val Shape2Xl = RoundedCornerShape(16.dp)
val Shape3Xl = RoundedCornerShape(24.dp)
val ShapePill = RoundedCornerShape(999.dp)

@Composable
fun StiMarkTile(
    size: Dp = 40.dp,
    contentDescription: String? = "STI"
) {
    Image(
        painter = painterResource(R.drawable.ic_sti_logo),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(6.dp))
    )
}

@Composable
fun StiMark(onDark: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        StiMarkTile()
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = stringResource(R.string.system_identity_line),
                color = if (onDark) Color.White else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                letterSpacing = (-0.01).sp
            )
            Text(
                text = stringResource(R.string.system_full_descriptor),
                color = if (onDark) {
                    Color.White.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = FontWeight.SemiBold,
                fontSize = 8.sp,
                letterSpacing = 0.35.sp
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
        if (eyebrow.isNotBlank()) {
            Text(
                text = eyebrow.uppercase(),
                style = EyebrowStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            .border(1.dp, MaterialTheme.colorScheme.outline, Shape2Xl)
            .padding(contentPadding)
    ) {
        content()
    }
}

/** ELMS-style section card alias */
@Composable
fun ElmsSectionCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) = SectionCard(modifier, contentPadding, content)

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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (note != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        note,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(ShapeXl)
                    .background(if (highlight) AccentYellow35 else ElmsCyanSoft)
                    .padding(10.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (highlight) StiMarkBlue else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun StatusBadge(label: String, warning: Boolean = false) {
    val background = if (warning) AccentYellow35 else ElmsCyanSoft
    Text(
        text = label,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(ShapePill)
            .background(background)
            .border(1.dp, MaterialTheme.colorScheme.outline, ShapePill)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
fun ElmsPillTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    count: Int? = null,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) ElmsCyan else Color.Transparent
    val fg = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = modifier
            .sizeIn(minHeight = 48.dp)
            .clip(ShapePill)
            .background(bg)
            .then(
                if (!selected) Modifier.border(1.dp, MaterialTheme.colorScheme.outline, ShapePill)
                else Modifier
            )
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Tab
                contentDescription = if (count != null) "$label, $count" else label
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = fg, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        if (count != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .sizeIn(minWidth = 22.dp, minHeight = 22.dp)
                    .clip(CircleShape)
                    .background(if (selected) Color.White else ElmsCyan),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count.toString(),
                    color = if (selected) ElmsCyan else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ElmsCountBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    if (count <= 0) return
    Box(
        modifier = modifier
            .sizeIn(minWidth = 18.dp, minHeight = 18.dp)
            .clip(CircleShape)
            .background(ElmsCyan),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun ElmsListRow(
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    leading: @Composable () -> Unit,
    trailing: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    subtitleMaxLines: Int = 1,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .sizeIn(minHeight = 64.dp)
            .then(
                if (onClick != null) {
                    Modifier
                        .clip(ShapeXl)
                        .clickable(onClick = onClick)
                        .semantics {
                            role = Role.Button
                            contentDescription = title
                        }
                } else Modifier
            )
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(ShapeXl)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            leading()
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    maxLines = subtitleMaxLines,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }
    }
}

enum class ElmsBannerTone { Error, Success, Info }

@Composable
fun ElmsBanner(
    message: String,
    tone: ElmsBannerTone = ElmsBannerTone.Error,
    modifier: Modifier = Modifier
) {
    val bg = when (tone) {
        ElmsBannerTone.Error -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
        ElmsBannerTone.Success -> SuccessGreenSoft
        ElmsBannerTone.Info -> ElmsCyanSoft
    }
    val fg = when (tone) {
        ElmsBannerTone.Error -> MaterialTheme.colorScheme.error
        ElmsBannerTone.Success -> Color(0xFF2B8A3E)
        ElmsBannerTone.Info -> ElmsCyan
    }
    Text(
        text = message,
        color = fg,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .fillMaxWidth()
            .clip(ShapeXl)
            .background(bg)
            .border(1.dp, fg.copy(alpha = 0.25f), ShapeXl)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    )
}

private val SuccessGreenSoft = Color(0x1A2B8A3E)

@Composable
fun ElmsEmptyState(
    title: String,
    description: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            PortalButton(text = actionLabel, onClick = onAction)
        }
    }
}

@Composable
fun ElmsLoadingBlock(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = ElmsCyan,
            modifier = Modifier.size(36.dp),
            strokeWidth = 3.dp
        )
    }
}

enum class PortalButtonVariant { Primary, Secondary, Ghost, Yellow }

@Composable
fun PortalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: PortalButtonVariant = PortalButtonVariant.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null
) {
    val background = when (variant) {
        PortalButtonVariant.Primary -> ElmsCyan
        PortalButtonVariant.Secondary -> MaterialTheme.colorScheme.surface
        PortalButtonVariant.Ghost -> Color.Transparent
        PortalButtonVariant.Yellow -> AccentYellow
    }
    val contentColor = when (variant) {
        PortalButtonVariant.Primary -> Color.White
        PortalButtonVariant.Yellow -> StiMarkBlue
        else -> MaterialTheme.colorScheme.onSurface
    }
    val borderColor = when (variant) {
        PortalButtonVariant.Secondary -> MaterialTheme.colorScheme.outline
        else -> Color.Transparent
    }
    val clickable = enabled && !loading

    Row(
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .clip(ShapeXl)
            .background(if (clickable) background else background.copy(alpha = 0.45f))
            .border(1.dp, borderColor, ShapeXl)
            .clickable(enabled = clickable, onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = text
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = contentColor,
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        } else if (leadingIcon != null) {
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
    trailingIcon: ImageVector? = null,
    error: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val borderColor = when {
        error != null -> MaterialTheme.colorScheme.error
        !enabled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        focused -> ElmsCyan
        else -> MaterialTheme.colorScheme.outline
    }
    val contentAlpha = if (enabled) 1f else 0.45f

    Column(modifier = modifier.fillMaxWidth()) {
        if (label.isNotEmpty()) {
            Text(
                label,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
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
                .background(
                    if (enabled) MaterialTheme.colorScheme.surface
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        placeholder,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f * contentAlpha),
                        fontSize = 14.sp
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    enabled = enabled,
                    readOnly = readOnly,
                    interactionSource = interaction,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    cursorBrush = SolidColor(ElmsCyan),
                    visualTransformation = visualTransformation,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (trailing != null || trailingIcon != null) {
                Box(
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (trailing != null) {
                        trailing()
                    } else if (trailingIcon != null) {
                        Icon(
                            trailingIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
