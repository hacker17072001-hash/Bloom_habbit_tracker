package com.example.ui.components

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.CategoryEntity
import com.example.data.entity.HabitEntity
import com.example.ui.theme.BloomPrimary
import com.example.ui.theme.BloomSoftLavender
import com.example.util.DateUtils

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

                Spacer(modifier = Modifier.height(16.dp))

                // Category Selection
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat.name,
                            onClick = { selectedCategory = cat.name },
                            label = { Text(cat.name, fontSize = 12.sp) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BloomSoftLavender,
                                selectedLabelColor = BloomPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Habit Type Selection
                Text(
                    text = "Tracking Type",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("SIMPLE" to "Simple Check", "QUANTITY" to "Quantity", "DURATION" to "Duration").forEach { (typeKey, label) ->
                        FilterChip(
                            selected = selectedType == typeKey,
                            onClick = {
                                selectedType = typeKey
                                if (typeKey == "DURATION") unit = "mins"
                                if (typeKey == "SIMPLE") unit = "times"
                            },
                            label = { Text(label, fontSize = 12.sp, maxLines = 1, softWrap = false) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BloomSoftLavender,
                                selectedLabelColor = BloomPrimary
                            )
                        )
                    }
                }

                // Target & Unit inputs if Quantity or Duration
                if (selectedType != "SIMPLE") {
                    Spacer(modifier = Modifier.height(12.dp))
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

                Spacer(modifier = Modifier.height(16.dp))

                // Schedule selection
                Text(
                    text = "Schedule Frequency",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "EVERY_DAY" to "Every Day",
                        "WEEKDAYS" to "Weekdays (Mon-Fri)",
                        "WEEKENDS" to "Weekends (Sat-Sun)"
                    ).forEach { (schedKey, label) ->
                        FilterChip(
                            selected = scheduleType == schedKey,
                            onClick = { scheduleType = schedKey },
                            label = { Text(label, fontSize = 12.sp) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BloomSoftLavender,
                                selectedLabelColor = BloomPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Preferred Time of Day
                Text(
                    text = "Time of Day",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
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
                                FilterChip(
                                    selected = preferredTime == time,
                                    onClick = { preferredTime = time },
                                    label = {
                                        Text(
                                            text = time,
                                            fontSize = 12.sp,
                                            fontWeight = if (preferredTime == time) FontWeight.Bold else FontWeight.Medium,
                                            maxLines = 1,
                                            softWrap = false,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BloomSoftLavender,
                                        selectedLabelColor = BloomPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Color Selection
                Text(
                    text = "Accent Color",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BloomIconHelper.availableColors.forEach { hex ->
                        val col = BloomIconHelper.parseColor(hex)
                        val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(col)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hex }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Icon Selection
                Text(
                    text = "Icon Symbol",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BloomIconHelper.availableIcons.forEach { (iconKey, label) ->
                        val isSelected = selectedIconName == iconKey
                        val icon = BloomIconHelper.getIcon(iconKey)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) BloomSoftLavender else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) BloomPrimary else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedIconName = iconKey },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) BloomPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save Button
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
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_habit_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BloomPrimary),
                    enabled = name.isNotBlank()
                ) {
                    Text(
                        text = if (initialHabit == null) "Create Habit" else "Save Changes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
