package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.KickEventEntity
import com.example.data.model.KickSessionEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarHistoryScreen(
    allSessions: List<KickSessionEntity>,
    selectedDateMillis: Long,
    selectedSessionEvents: List<KickEventEntity>,
    onSelectDate: (Long) -> Unit,
    onLoadSessionEvents: (Long) -> Unit,
    onDeleteSession: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // Current viewed month offset
    var monthOffset by remember { mutableIntStateOf(0) }

    val calendar = remember(monthOffset) {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, monthOffset)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val monthTitle = monthYearFormat.format(calendar.time)

    // Calculate calendar grid days
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0-based for Sunday

    // Filter sessions for selected date
    val selCal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
    val selYear = selCal.get(Calendar.YEAR)
    val selDayOfYear = selCal.get(Calendar.DAY_OF_YEAR)

    val sessionsForDay = remember(allSessions, selectedDateMillis) {
        allSessions.filter {
            val c = Calendar.getInstance().apply { timeInMillis = it.startTimeMs }
            c.get(Calendar.YEAR) == selYear && c.get(Calendar.DAY_OF_YEAR) == selDayOfYear
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Navigation Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { monthOffset-- },
                            modifier = Modifier.testTag("prev_month_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                        }

                        Text(
                            text = monthTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        IconButton(
                            onClick = { monthOffset++ },
                            modifier = Modifier.testTag("next_month_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Day of Week Header
                    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        daysOfWeek.forEach { day ->
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Days Grid
                    val totalSlots = firstDayOfWeek + daysInMonth
                    val weeksCount = (totalSlots + 6) / 7

                    for (week in 0 until weeksCount) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            for (dayCol in 0..6) {
                                val slotIndex = week * 7 + dayCol
                                val dayNumber = slotIndex - firstDayOfWeek + 1

                                if (dayNumber in 1..daysInMonth) {
                                    val cellCal = Calendar.getInstance().apply {
                                        time = calendar.time
                                        set(Calendar.DAY_OF_MONTH, dayNumber)
                                    }
                                    val cellTimeMs = cellCal.timeInMillis
                                    val isSelected = cellCal.get(Calendar.YEAR) == selYear &&
                                            cellCal.get(Calendar.DAY_OF_YEAR) == selDayOfYear

                                    val sessionsOnThisDay = allSessions.filter {
                                        val c = Calendar.getInstance().apply { timeInMillis = it.startTimeMs }
                                        c.get(Calendar.YEAR) == cellCal.get(Calendar.YEAR) &&
                                                c.get(Calendar.DAY_OF_YEAR) == cellCal.get(Calendar.DAY_OF_YEAR)
                                    }

                                    CalendarDayCell(
                                        dayNumber = dayNumber,
                                        isSelected = isSelected,
                                        hasSessions = sessionsOnThisDay.isNotEmpty(),
                                        allMetTarget = sessionsOnThisDay.isNotEmpty() && sessionsOnThisDay.all { it.isTargetReached },
                                        onClick = { onSelectDate(cellTimeMs) },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected Date Label
        item {
            val selectedDateFmt = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = selectedDateFmt.format(Date(selectedDateMillis)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Sessions list for selected date
        if (sessionsForDay.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No kick sessions recorded on this day.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(sessionsForDay, key = { it.id }) { session ->
                SessionHistoryCard(
                    session = session,
                    events = selectedSessionEvents,
                    onLoadEvents = { onLoadSessionEvents(session.id) },
                    onDelete = { onDeleteSession(session.id) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun CalendarDayCell(
    dayNumber: Int,
    isSelected: Boolean,
    hasSessions: Boolean,
    allMetTarget: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(2.dp)
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .testTag("calendar_day_$dayNumber"),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$dayNumber",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )

            // Activity dot
            if (hasSessions) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color.White
                            else if (allMetTarget) Color(0xFF2E7D32) // Emerald Green
                            else Color(0xFFE5A93B) // Amber
                        )
                )
            } else {
                Spacer(modifier = Modifier.size(5.dp))
            }
        }
    }
}

@Composable
fun SessionHistoryCard(
    session: KickSessionEntity,
    events: List<KickEventEntity>,
    onLoadEvents: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val startTimeStr = timeFormat.format(Date(session.startTimeMs))
    val mins = session.durationSeconds / 60
    val secs = session.durationSeconds % 60

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (session.isTargetReached) Color(0xFF2E7D32) else Color(0xFFE5A93B)
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = startTimeStr,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp).testTag("delete_session_${session.id}")) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Session",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Duration",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${mins}m ${secs}s",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column {
                    Text(
                        text = "Kicks Counted",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (session.targetKicks > 0) "${session.totalKicks} / ${session.targetKicks}" else "${session.totalKicks}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column {
                    Text(
                        text = "Strength",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val strLabel = when (session.movementStrength) {
                        3 -> "Vigorous"
                        2 -> "Moderate"
                        else -> "Gentle"
                    }
                    Text(
                        text = strLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (session.waterIntakeGlasses > 0) {
                    Column {
                        Text(
                            text = "Water",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "💧 ${session.waterIntakeGlasses} gls",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1976D2)
                        )
                    }
                }
            }

            if (session.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Notes: ${session.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Expandable kick timestamp log
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expanded = !expanded
                        if (expanded) onLoadEvents()
                    }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (expanded) "Hide Movement Timestamps" else "View Movement Timestamps",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    val eventTimeFmt = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
                    val sessionEvents = events.filter { it.sessionId == session.id }

                    if (sessionEvents.isEmpty()) {
                        Text(
                            text = "Loading timestamps...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        sessionEvents.forEachIndexed { idx, ev ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Kick #${idx + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = eventTimeFmt.format(Date(ev.timestampMs)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "+${ev.intervalSincePreviousSeconds}s",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
