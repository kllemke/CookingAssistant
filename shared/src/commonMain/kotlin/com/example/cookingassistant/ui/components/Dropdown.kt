package com.example.cookingassistant.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.cookingassistant.ui.theme.Dimens
import cookingassistant.shared.generated.resources.Res
import cookingassistant.shared.generated.resources.ic_check
import cookingassistant.shared.generated.resources.ic_keyboard_arrow_down
import cookingassistant.shared.generated.resources.ic_keyboard_arrow_up
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * @param caption small label above the value.
 * @param value current selection shown in the field.
 * @param icon leading icon of the field.
 * @param expanded whether the menu is currently open.
 * @param highlighted whether the field is highlighted.
 * @param onExpandedChange called when the menu is opened or closed.
 * @param content menu entries.
 */
@Composable
internal fun Dropdown(
    caption: String,
    value: String,
    icon: DrawableResource,
    expanded: Boolean,
    highlighted: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier) {
        DropdownField(
            caption = caption,
            value = value,
            icon = icon,
            expanded = expanded,
            highlighted = highlighted,
            onClick = { onExpandedChange(!expanded) },
            modifier = Modifier
                .fillMaxWidth()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            offset = DpOffset(0.dp, Dimens.spacingExtraSmall),
            shape = MaterialTheme.shapes.small,
            containerColor = MaterialTheme.colorScheme.surface,
            shadowElevation = Dimens.shadowElevation,
            border = BorderStroke(Dimens.borderWidth, MaterialTheme.colorScheme.secondary),
            modifier = Modifier.heightIn(max = Dimens.dropdownMaxHeight),
            content = {
                Column(modifier = Modifier.padding(horizontal = Dimens.menuGutter)) {
                    content()
                }
            },
        )
    }
}

/**
 * The closed state of a [Dropdown]
 *
 * @param caption small label above the value.
 * @param value current selection.
 * @param icon leading icon.
 * @param expanded whether the menu is open.
 * @param highlighted whether the field is highlighted.
 * @param onClick called when the field is tapped (toggles the menu).
 */
@Composable
private fun DropdownField(
    caption: String,
    value: String,
    icon: DrawableResource,
    expanded: Boolean,
    highlighted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = if (highlighted) colors.secondary else colors.surface,
        border = BorderStroke(Dimens.borderWidth, if (expanded || highlighted) colors.primary else colors.secondary),
        modifier = modifier
            .height(Dimens.controlHeight)
            .semantics { stateDescription = if (expanded) "geöffnet" else "geschlossen" },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.controlGap),
            modifier = Modifier.padding(horizontal = Dimens.controlInset),
        ) {
            Icon(painterResource(icon), contentDescription = null, tint = colors.primary, modifier = Modifier.size(Dimens.iconSizeMedium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    caption,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                painterResource(if (expanded) Res.drawable.ic_keyboard_arrow_up else Res.drawable.ic_keyboard_arrow_down),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Dimens.iconSizeMedium),
            )
        }
    }
}

/**
 * Shared menu row: every row inside a [Dropdown] menu uses this container,
 * so rows align structurally instead of by matched paddings. The gutter
 * between the menu surface and its rows is applied by the [Dropdown]
 * container, not here. [modifier] applies inside the clip (e.g.
 * [selectable] or [toggleable] plus a background) so it stays clipped to
 * the row radius.
 *
 * @param modifier row behavior and background, applied inside the clip.
 * @param content row entries.
 */
@Composable
internal fun DropdownMenuRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.menuRowGap),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.controlHeight)
            .clip(MaterialTheme.shapes.small)
            .then(modifier)
            .padding(horizontal = Dimens.menuRowPadding),
        content = content,
    )
}

/**
 * Single-choice menu entry: the selected row gets a tinted background and a check mark.
 *
 * @param label text of the entry.
 * @param selected whether this entry is the current choice.
 * @param onClick called when the entry is tapped.
 */
@Composable
internal fun OptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    DropdownMenuRow(
        modifier = Modifier
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(
                painterResource(Res.drawable.ic_check),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Dimens.iconSizeLarge),
            )
        } else {
            Spacer(Modifier.size(Dimens.iconSizeLarge))
        }
    }
}

/**
 * Multi-choice menu entry with a checkbox. The whole row is the touch target.
 *
 * @param label text of the entry.
 * @param checked whether the entry is currently selected.
 * @param onToggle called when the row is tapped; the caller flips the state.
 */
@Composable
internal fun DropdownCheckRow(label: String, checked: Boolean, onToggle: () -> Unit) {
    DropdownMenuRow(
        modifier = Modifier.toggleable(value = checked, role = Role.Checkbox, onValueChange = { onToggle() }),
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
        )
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}
