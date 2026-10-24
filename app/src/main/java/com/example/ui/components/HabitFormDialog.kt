package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.CategoryEntity
import com.example.data.entity.HabitEntity
import com.example.ui.theme.BloomPrimary
import com.example.ui.theme.BloomSoftLavender
import com.example.util.DateUtils

/**
 * Modern, tactile selectable button for option groups in the habit form.
 * Ensures buttons have clear backgrounds, distinct borders, responsive ripple,
 * and high contrast in both selected and unselected states.
 */
@Composable
fun BloomSelectableButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showCheckmark: Boolean = true,
    height: Dp = 40.dp,
    testTag: String? = null
) {
    val containerColor = if (selected) {
        BloomSoftLavender
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    }
    val contentColor = if (selected) {
        BloomPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val borderColor = if (selected) {
        BloomPrimary
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.85f)
    }
    val borderWidth = if (selected) 1.5.dp else 1.dp

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(height)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(borderWidth, borderColor),
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected && showCheckmark) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = BloomPrimary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = contentColor,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HabitFormDialog(
    initialHabit: HabitEntity? = null,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (HabitEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialHabit?.name ?: "") }
    var description by remember { mutableStateOf(initialHabit?.description ?: "") }
    var selectedCategory by remember { mutableStateOf(initialHabit?.categoryId ?: "Productivity") }
    var selectedType by remember { mutableStateOf(initialHabit?.type ?: "SIMPLE") }
    var targetValue by remember { mutableDoubleStateOf(initialHabit?.targetValue ?: 1.0) }
    var unit by remember { mutableStateOf(initialHabit?.unit ?: "times") }
    var scheduleType by remember { mutableStateOf(initialHabit?.scheduleType ?: "EVERY_DAY") }
    var selectedColorHex by remember { mutableStateOf(initialHabit?.colorHex ?: "#6C5CE7") }
    var selectedIconName by remember { mutableStateOf(initialHabit?.iconName ?: "check") }
    var preferredTime by remember { mutableStateOf(initialHabit?.preferredTime ?: "Anytime") }

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
                .clip(RoundedCornerShape(28.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(scrollState)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialHabit == null) "New Habit" else "Edit Habit",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Habit Name") },
                    placeholder = { Text("e.g. Read 20 Pages") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("habit_name_input"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BloomPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("Why is this habit meaningful to you?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BloomPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Category Selection
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        BloomSelectableButton(
                            text = cat.name,
                            selected = selectedCategory == cat.name,
                            onClick = { selectedCategory = cat.name },
                            showCheckmark = true,
                            height = 38.dp,
                            testTag = "category_button_${cat.name.lowercase().replace(" ", "_")}"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Habit Type Selection
                Text(
                    text = "Tracking Type",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "SIMPLE" to "Simple Check",
                        "QUANTITY" to "Quantity",
                        "DURATION" to "Duration"
                    ).forEach { (typeKey, label) ->
                        BloomSelectableButton(
                            text = label,
                            selected = selectedType == typeKey,
                            onClick = {
                                selectedType = typeKey
                                if (typeKey == "DURATION") unit = "mins"
                                if (typeKey == "SIMPLE") unit = "times"
                            },
                            showCheckmark = false,
                            height = 44.dp,
                            modifier = Modifier.weight(1f),
                            testTag = "type_button_$typeKey"
                        )
                    }
                }

                // Target & Unit inputs if Quantity or Duration
                if (selectedType != "SIMPLE") {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = if (targetValue > 0) targetValue.toInt().toString() else "",
                            onValueChange = { targetValue = it.toDoubleOrNull() ?: 1.0 },
                            label = { Text("Target Goal") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unit") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Schedule selection
                Text(
                    text = "Schedule Frequency",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "EVERY_DAY" to "Every Day",
                        "WEEKDAYS" to "Weekdays (Mon-Fri)",
                        "WEEKENDS" to "Weekends (Sat-Sun)"
                    ).forEach { (schedKey, label) ->
                        BloomSelectableButton(
                            text = label,
                            selected = scheduleType == schedKey,
                            onClick = { scheduleType = schedKey },
                            showCheckmark = true,
                            height = 38.dp,
                            testTag = "schedule_button_$schedKey"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Preferred Time of Day (2x2 balanced button grid)
                Text(
                    text = "Time of Day",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val timeRows = listOf(
                        listOf("Morning", "Afternoon"),
                        listOf("Evening", "Anytime")
                    )
                    timeRows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { time ->
                                BloomSelectableButton(
                                    text = time,
                                    selected = preferredTime == time,
                                    onClick = { preferredTime = time },
                                    showCheckmark = true,
                                    height = 44.dp,
                                    modifier = Modifier.weight(1f),
                                    testTag = "time_button_${time.lowercase()}"
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Color Selection
                Text(
                    text = "Accent Color",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BloomIconHelper.availableColors.forEach { hex ->
                        val col = BloomIconHelper.parseColor(hex)
                        val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(col)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Icon Selection
                Text(
                    text = "Icon Symbol",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BloomIconHelper.availableIcons.forEach { (iconKey, label) ->
                        val isSelected = selectedIconName == iconKey
                        val icon = BloomIconHelper.getIcon(iconKey)
                        Surface(
                            onClick = { selectedIconName = iconKey },
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) BloomSoftLavender else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) BloomPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.85f)
                            ),
                            tonalElevation = if (isSelected) 2.dp else 0.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) BloomPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Dialog Action Buttons: Cancel and Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            text = "Cancel",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val habit = (initialHabit ?: HabitEntity(name = name.trim(), startDate = DateUtils.today())).copy(
                                    name = name.trim(),
                                    description = description.trim(),
                                    categoryId = selectedCategory,
                                    type = selectedType,
                                    targetValue = targetValue,
                                    unit = unit,
                                    scheduleType = scheduleType,
                                    colorHex = selectedColorHex,
                                    iconName = selectedIconName,
                                    preferredTime = preferredTime,
                                    updatedAt = System.currentTimeMillis()
                                )
                                onSave(habit)
                            }
                        },
                        modifier = Modifier
                            .weight(1.4f)
                            .height(52.dp)
                            .testTag("save_habit_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BloomPrimary),
                        enabled = name.isNotBlank()
                    ) {
                        Text(
                            text = if (initialHabit == null) "Create Habit" else "Save Changes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
