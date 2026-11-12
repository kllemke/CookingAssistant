package com.example.cookingassistant.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            offset = DpOffset(0.dp, 4.dp),
            shape = RoundedCornerShape(8.dp),
            containerColor = Color.White,
            shadowElevation = 8.dp,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
            modifier = Modifier.heightIn(max = 420.dp),
            content = content,
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
        shape = RoundedCornerShape(8.dp),
        color = if (highlighted) colors.secondary else colors.surface,
        border = BorderStroke(2.dp, if (expanded || highlighted) colors.primary else colors.secondary),
        modifier = modifier
            .height(48.dp)
            .semantics { stateDescription = if (expanded) "geöffnet" else "geschlossen" },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 12.dp),
        ) {
            Icon(painterResource(icon), contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(caption, fontSize = 11.sp, lineHeight = 14.sp, color = Color.Gray)
                Text(
                    value,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                painterResource(if (expanded) Res.drawable.ic_keyboard_arrow_up else Res.drawable.ic_keyboard_arrow_down),
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(18.dp),
            )
        }
    }
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(10.dp))
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .background(if (selected) MaterialTheme.colorScheme.surface else Color.White)
            .padding(horizontal = 10.dp),
    ) {
        Text(
            label,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = Color.Black,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(
                painterResource(Res.drawable.ic_check),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        } else {
            Spacer(Modifier.size(20.dp))
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(10.dp))
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(horizontal = 10.dp),
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
        )
        Text(label, fontSize = 15.sp, color = Color.Black, modifier = Modifier.weight(1f))
    }
}
