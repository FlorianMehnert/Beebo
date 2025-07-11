package com.fm.beebo.ui.search

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.fm.beebo.models.LibraryMedia
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@Composable
fun SearchResultItem(
    item: LibraryMedia,
    onClick: () -> Unit
) {

    var title = item.title.replace("¬", "")

    val dueDate = if (item.dueDates.isNotEmpty()) item.dueDates[0] else ""

    val displayMedium = item.kindOfMedium.getIcon()
    var showMediumTooltip by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (item.isAvailable) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.surface)
                    .clickable { showMediumTooltip = true },  // Added clickable modifier here
                contentAlignment = Alignment.Center
            ) {
                // icon display for kindOfMedium
                Text(
                    text = displayMedium,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.year.isNotEmpty()) {
                        Text(
                            text = item.year,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    if (dueDate.isNotEmpty()) {
                        Text(
                            text = buildAnnotatedString {
                                append("Entliehen bis: ")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(dueDate)
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }else if (item.isAvailable){
                        Text(
                            text = "Verfügbar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
            }
        }
    }

    // Medium type tooltip/popup
    if (showMediumTooltip) {
        AlertDialog(
            onDismissRequest = { showMediumTooltip = false },
            title = { Text("Medienart") },
            text = {
                Column {
                    Text("$displayMedium steht für \"${item.kindOfMedium.getChipString()}\"")
                    Text("Im Moment ${if (item.isAvailable) "ausleihbar" else "nicht ausleihbar"}")
                }
            },
            confirmButton = {
                TextButton(onClick = { showMediumTooltip = false }) {
                    Text("Schließen")
                }
            }
        )
    }
}

fun addReminderToCalendar(context: Context, title: String, dueDate: String) {
    // Check for permissions first
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) !=
        PackageManager.PERMISSION_GRANTED
    ) {
        Toast.makeText(context, "Calendar permission required", Toast.LENGTH_SHORT).show()
        return
    }

    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val calendar = Calendar.getInstance()

    try {
        val date = dateFormat.parse(dueDate)
        if (date != null) {
            calendar.time = date
            // Set time to 8:00 AM
            calendar.set(Calendar.HOUR_OF_DAY, 8)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            // Get the default calendar ID
            val calendarId = getDefaultCalendarId(context)
            if (calendarId == -1L) {
                Toast.makeText(context, "No calendar found", Toast.LENGTH_SHORT).show()
                return
            }

            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.TITLE, "Medium verfügbar: $title")
                put(CalendarContract.Events.DESCRIPTION, "Erinnerung: $title könnte verfügbar sein")
                put(CalendarContract.Events.DTSTART, calendar.timeInMillis)
                put(
                    CalendarContract.Events.DTEND,
                    calendar.timeInMillis + 7200000
                ) // End time is 1 hour later
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                put(CalendarContract.Events.HAS_ALARM, 1)
            }

            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)

            if (uri != null) {
                // Add a reminder to the event
                val reminderValues = ContentValues().apply {
                    put(CalendarContract.Reminders.EVENT_ID, ContentUris.parseId(uri))
                    put(CalendarContract.Reminders.MINUTES, 60) // Reminder 1 hour before
                    put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                }
                context.contentResolver.insert(
                    CalendarContract.Reminders.CONTENT_URI,
                    reminderValues
                )

                Toast.makeText(context, "Erinnerung um 8 Uhr hinzugefügt", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(
                    context,
                    "Konnte die Erinnerung nicht hinzufügen",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Fehler: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

// Helper function to get the default calendar ID
private fun getDefaultCalendarId(context: Context): Long {
    val projection = arrayOf(CalendarContract.Calendars._ID)
    val selection =
        "${CalendarContract.Calendars.VISIBLE} = ? AND ${CalendarContract.Calendars.IS_PRIMARY} = ?"
    val selectionArgs = arrayOf("1", "1")

    context.contentResolver.query(
        CalendarContract.Calendars.CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            return cursor.getLong(0)
        }
    }

    // If no primary calendar, get the first available
    context.contentResolver.query(
        CalendarContract.Calendars.CONTENT_URI,
        projection,
        "${CalendarContract.Calendars.VISIBLE} = 1",
        null,
        null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            return cursor.getLong(0)
        }
    }

    return -1L
}
