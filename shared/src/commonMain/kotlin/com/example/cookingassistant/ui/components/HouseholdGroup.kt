package com.example.cookingassistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import com.example.cookingassistant.ui.screenmodel.Person
import com.example.cookingassistant.ui.theme.Dimens

@Composable
fun HouseholdGroup(
    persons: List<Person>,
    selected: Person?,
    onSelectPerson: (Person) -> Unit,
    onToggleIncluded: (Person) -> Unit,
    onAddPerson: (name: String) -> Unit,
    onRenamePerson: (person: Person, newName: String) -> Unit,
    onRemovePerson: (Person) -> Unit,
    getNameError: (name: String, currentName: String?) -> String?,
    modifier: Modifier = Modifier,
) {
    var edit by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.menuRowGap),
    ) {
        Text(
            text = "Household",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .border(
                    shape = MaterialTheme.shapes.small,
                    width = Dimens.borderWidth,
                    color = MaterialTheme.colorScheme.outline,
                ),
        ) {
            for (person in persons) {
                if (edit == person.name) {
                    PersonNameEdit(
                        initialName = person.name,
                        getError = { getNameError(it, person.name) },
                        onSave = { newName ->
                            onRenamePerson(person, newName)
                            edit = null
                        },
                        secondaryAction = {
                            IconButton(
                                onClick = {
                                    edit = null
                                    onRemovePerson(person)
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove ${person.name}",
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        },
                    )
                } else {
                    PersonRow(
                        person = person,
                        isSelected = person.name == selected?.name,
                        onSelect = { onSelectPerson(person) },
                        onEdit = {
                            onSelectPerson(person)
                            edit = person.name
                        },
                        onToggleIncluded = { onToggleIncluded(person) },
                    )
                }
                HorizontalDivider(
                    thickness = Dimens.hairlineWidth,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
            if (edit == "") {
                PersonNameEdit(
                    initialName = "",
                    getError = { getNameError(it, null) },
                    onSave = { name ->
                        onAddPerson(name)
                        edit = null
                    },
                    secondaryAction = {
                        IconButton(onClick = { edit = null }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                )
            } else {
                AddPersonRow(onClick = { edit = "" })
            }
        }
    }
}

@Composable
private fun PersonRow(
    person: Person,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onToggleIncluded: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.personRowHeight)
            .background(if (isSelected) MaterialTheme.colorScheme.surfaceContainer else Color.Transparent),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.personRowLineGap, Alignment.CenterVertically),
            modifier = Modifier
                .weight(1f)
                .heightIn(min = Dimens.personRowHeight)
                .semantics { selected = isSelected }
                .clickable(role = Role.Button, onClick = onSelect)
                .padding(horizontal = Dimens.menuRowPadding),
        ) {
            Text(
                text = person.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (person.included) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text =  if (person.intolerances.isEmpty())  "No intolerances" else person.intolerances.joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Rename ${person.name}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Dimens.iconSizeLarge),
            )
        }
        Checkbox(
            checked = person.included,
            onCheckedChange = { onToggleIncluded() },
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.tertiary),
        )
    }
}

@Composable
private fun PersonNameEdit(
    initialName: String,
    getError: (String) -> String?,
    onSave: (String) -> Unit,
    secondaryAction: @Composable () -> Unit,
) {
    var value by remember { mutableStateOf(TextFieldValue(initialName, TextRange(initialName.length))) }
    val focusRequester = remember { FocusRequester() }
    val currentError = getError(value.text)
    val save = { if (currentError == null) onSave(value.text.trim()) }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingExtraSmall),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(
                start = Dimens.menuRowPadding,
                end = Dimens.spacingExtraSmall,
                top = Dimens.controlGap,
                bottom = Dimens.controlGap,
            ),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                value = it
            },
            placeholder = { Text("Name") },
            singleLine = true,
            isError = currentError != null,
            supportingText = currentError?.let { { Text(it) } },
            textStyle = MaterialTheme.typography.bodyMedium,
            shape = MaterialTheme.shapes.small,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done,
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.outline,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.heightIn(min = Dimens.textFieldHeight),
        ) {
            secondaryAction()
            FilledIconButton(
                onClick = save,
                enabled = currentError == null,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                ),
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Save name")
            }
        }
    }
}

@Composable
private fun AddPersonRow(onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.controlGap),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.controlHeight)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = Dimens.menuRowPadding),
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(Dimens.iconSizeLarge),
        )
        Text(
            text = "Add person",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.tertiary,
        )
    }
}