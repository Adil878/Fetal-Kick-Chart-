package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ripple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ActiveSessionState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActiveSessionScreen(
    state: ActiveSessionState,
    onTargetChange: (Int) -> Unit,
    onDurationChange: (Int?) -> Unit,
    onStartSession: () -> Unit,
    onRecordKick: () -> Unit,
    onUndoKick: () -> Unit,
    onTogglePause: () -> Unit,
    onRequestFinish: () -> Unit,
    onCancelSession: () -> Unit,
    onUpdateNotes: (String) -> Unit,
    onUpdateStrength: (Int) -> Unit,
    onSaveCompleted: () -> Unit,
    onAddWaterGlass: () -> Unit,
    onRemoveWaterGlass: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!state.isRunning) {
            // Configuration & Setup Mode
            SessionConfigurationCard(
                selectedTargetKicks = state.selectedTargetKicks,
                selectedDurationMinutes = state.selectedDurationMinutes,
                onTargetChange = onTargetChange,
                onDurationChange = onDurationChange,
                onStartSession = onStartSession
            )
        } else {
            // Live Tracking Mode
            ActiveTrackingControls(
                state = state,
                onRecordKick = onRecordKick,
                onUndoKick = onUndoKick,
                onTogglePause = onTogglePause,
                onRequestFinish = onRequestFinish,
                onCancelSession = onCancelSession,
                onAddWaterGlass = onAddWaterGlass,
                onRemoveWaterGlass = onRemoveWaterGlass
            )
        }
    }

    // Finish & Save Session Dialog
    if (state.showFinishDialog) {
        FinishSessionDialog(
            state = state,
            onNotesChange = onUpdateNotes,
            onStrengthChange = onUpdateStrength,
            onSave = onSaveCompleted,
            onDismiss = onCancelSession
        )
    }
}

@Composable
fun SessionConfigurationCard(
    selectedTargetKicks: Int,
    selectedDurationMinutes: Int?,
    onTargetChange: (Int) -> Unit,
    onDurationChange: (Int?) -> Unit,
    onStartSession: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "New Movement Session",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Customize your target kicks and time window to match your doctor's instructions.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // Target Kicks Option
            Text(
                text = "Target Kick Goal",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val targets = listOf(
                    5 to "5 Kicks",
                    10 to "10 (Standard)",
                    15 to "15 Kicks",
                    20 to "20 Kicks",
                    0 to "Freeform"
                )
                targets.forEach { (count, label) ->
                    val isSelected = selectedTargetKicks == count
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTargetChange(count) },
                        label = { Text(if (count == 0) "Open" else "$count") },
                        modifier = Modifier.testTag("target_chip_$count")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Duration Limit Option (Customizable)
            Text(
                text = "Session Duration Limit",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val durations = listOf(
                    30 to "30m",
                    60 to "1 hr",
                    120 to "2 hr",
                    180 to "3 hr",
                    null to "Untimed"
                )
                durations.forEach { (mins, label) ->
                    val isSelected = selectedDurationMinutes == mins
                    FilterChip(
                        selected = isSelected,
                        onClick = { onDurationChange(mins) },
                        label = { Text(label) },
                        modifier = Modifier.testTag("duration_chip_${mins ?: "untimed"}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onStartSession,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("start_session_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start Kick Session",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActiveTrackingControls(
    state: ActiveSessionState,
    onRecordKick: () -> Unit,
    onUndoKick: () -> Unit,
    onTogglePause: () -> Unit,
    onRequestFinish: () -> Unit,
    onCancelSession: () -> Unit,
    onAddWaterGlass: () -> Unit,
    onRemoveWaterGlass: () -> Unit
) {
    // Pulse animation for the kick button
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (state.isPaused) 1.0f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Elapsed / Remaining Time Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                val totalSecs = state.elapsedSeconds
                val hours = totalSecs / 3600
                val mins = (totalSecs % 3600) / 60
                val secs = totalSecs % 60
                val formattedElapsed = String.format(Locale.US, "%02d:%02d:%02d", hours, mins, secs)

                Text(
                    text = if (state.isPaused) "Session Paused" else "Session Active",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (state.isPaused) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formattedElapsed,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                if (state.selectedDurationMinutes != null) {
                    val remSecs = state.remainingSeconds ?: 0L
                    val remMins = remSecs / 60
                    Text(
                        text = "Time Remaining",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${remMins}m left",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    Text(
                        text = "Mode",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Untimed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(28.dp))

    // Interactive Kick Button
    Box(
        modifier = Modifier
            .size(240.dp)
            .scale(pulseScale),
        contentAlignment = Alignment.Center
    ) {
        // Outer decorative pulse rings
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
        )
        Box(
            modifier = Modifier
                .size(208.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
        )

        // Main Tap Circle
        Box(
            modifier = Modifier
                .size(176.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                        )
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = Color.White),
                    onClick = onRecordKick
                )
                .testTag("record_kick_button"),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Record fetal movement",
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "+1 KICK",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "Tap for movement",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Progress to Target
    val targetText = if (state.selectedTargetKicks > 0) {
        "${state.currentKicks} / ${state.selectedTargetKicks} Kicks"
    } else {
        "${state.currentKicks} Movements"
    }

    Text(
        text = targetText,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )

    if (state.selectedTargetKicks > 0) {
        val progress = (state.currentKicks.toFloat() / state.selectedTargetKicks).coerceIn(0f, 1f)
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(vertical = 8.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }

    // Velocity & Average Interval Info
    if (state.averageIntervalSeconds > 0) {
        val avgMins = state.averageIntervalSeconds / 60
        val avgSecs = state.averageIntervalSeconds % 60
        val avgStr = if (avgMins > 0) "${avgMins}m ${avgSecs}s" else "${avgSecs}s"
        Text(
            text = "Average interval: $avgStr between kicks",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Action Control Buttons Row (Undo, Pause, Finish, Discard)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Undo button
        IconButton(
            onClick = onUndoKick,
            enabled = state.currentKicks > 0,
            modifier = Modifier.testTag("undo_kick_button")
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Undo,
                    contentDescription = "Undo last kick",
                    tint = if (state.currentKicks > 0) MaterialTheme.colorScheme.onSurface else Color.Gray
                )
            }
        }

        // Pause/Resume button
        IconButton(
            onClick = onTogglePause,
            modifier = Modifier.testTag("pause_resume_button")
        ) {
            Icon(
                imageVector = if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                contentDescription = if (state.isPaused) "Resume" else "Pause",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // Manual Finish Button
        Button(
            onClick = onRequestFinish,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
            modifier = Modifier.testTag("finish_early_button")
        ) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Finish")
        }

        // Cancel / Discard
        IconButton(
            onClick = onCancelSession,
            modifier = Modifier.testTag("cancel_session_button")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cancel session",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Hydration Tracker Card
    HydrationTrackerCard(
        glasses = state.waterGlasses,
        onAddGlass = onAddWaterGlass,
        onRemoveGlass = onRemoveWaterGlass
    )

    // Recent Kick Timestamps Badges
    if (state.kickTimestamps.isNotEmpty()) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Movement Timeline",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        )

        val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            state.kickTimestamps.takeLast(10).reversed().forEachIndexed { index, ts ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "#${state.kickTimestamps.size - index}: ${timeFormat.format(Date(ts))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun FinishSessionDialog(
    state: ActiveSessionState,
    onNotesChange: (String) -> Unit,
    onStrengthChange: (Int) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.isTargetReached) "Target Reached!" else "Session Summary",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                val mins = state.elapsedSeconds / 60
                val secs = state.elapsedSeconds % 60
                val durText = "${mins}m ${secs}s"

                Text(
                    text = "You recorded ${state.currentKicks} fetal movements in $durText.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (state.waterGlasses > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "💧 Hydration: ${state.waterGlasses} glass${if (state.waterGlasses > 1) "es" else ""} (${state.waterGlasses * 250} ml logged)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1976D2),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Movement Strength",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val strengths = listOf(
                        1 to "Gentle",
                        2 to "Moderate",
                        3 to "Vigorous"
                    )
                    strengths.forEach { (level, label) ->
                        FilterChip(
                            selected = state.movementStrength == level,
                            onClick = { onStrengthChange(level) },
                            label = { Text(label) },
                            modifier = Modifier.testTag("strength_chip_$level")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.notes,
                    onValueChange = onNotesChange,
                    label = { Text("Session Notes (e.g. after meal, lying down)") },
                    placeholder = { Text("Add any notes for your doctor...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("session_notes_input"),
                    minLines = 2,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("save_session_button")
            ) {
                Text("Save Session")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("discard_session_button")
            ) {
                Text("Discard")
            }
        }
    )
}

@Composable
fun HydrationTrackerCard(
    glasses: Int,
    onAddGlass: () -> Unit,
    onRemoveGlass: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2196F3).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = "Hydration",
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Session Hydration",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (glasses > 0) "$glasses glass${if (glasses > 1) "es" else ""} (${glasses * 250} ml)" else "0 ml logged (cold water can spur kicks)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (glasses > 0) {
                    IconButton(
                        onClick = onRemoveGlass,
                        modifier = Modifier.size(36.dp).testTag("remove_water_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Remove water glass",
                            tint = Color(0xFF1976D2)
                        )
                    }
                }
                Button(
                    onClick = onAddGlass,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_water_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+1 Glass", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

