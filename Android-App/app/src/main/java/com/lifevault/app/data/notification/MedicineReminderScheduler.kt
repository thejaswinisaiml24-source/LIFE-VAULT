package com.lifevault.app.data.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.lifevault.app.MainActivity
import com.lifevault.app.R
import java.util.Calendar

private const val CHANNEL_ID = "medicine_reminders"
private const val EXTRA_MEDICINE_NAME = "medicine_name"
private const val EXTRA_MEDICINE_DOSE = "medicine_dose"
private const val EXTRA_REQUEST_CODE = "request_code"

/**
 * Real scheduled local notifications via AlarmManager — not a fake toast.
 * Limitations, stated honestly: this only fires while the alarm survives
 * (exact alarms can be affected by battery optimization on some OEMs), and
 * schedules are NOT re-registered after a device reboot (no boot receiver
 * wired up yet) — so a reminder set today won't survive a phone restart
 * without the app being reopened at least once after boot.
 */
object MedicineReminderScheduler {

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Medicine Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminds you when it's time to take a medicine"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    /**
     * [timeText] expected as "HH:mm" in 24-hour form, or a 12-hour string
     * like "08:00 AM" — this parses common formats; if parsing fails, no
     * alarm is scheduled (caller should surface that to the user rather
     * than silently pretending it worked).
     */
    fun schedule(context: Context, medicineId: String, name: String, dose: String, timeText: String): Boolean {
        val calendar = parseTimeToCalendar(timeText) ?: return false

        val intent = Intent(context, MedicineReminderReceiver::class.java).apply {
            putExtra(EXTRA_MEDICINE_NAME, name)
            putExtra(EXTRA_MEDICINE_DOSE, dose)
            putExtra(EXTRA_REQUEST_CODE, medicineId.hashCode())
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicineId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
            return true
        } catch (e: SecurityException) {
            // Exact alarm permission not granted on Android 12+ — caller
            // should prompt the user to enable it in system settings.
            return false
        }
    }

    fun cancel(context: Context, medicineId: String) {
        val intent = Intent(context, MedicineReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicineId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    private fun parseTimeToCalendar(timeText: String): Calendar? {
        val trimmed = timeText.trim().uppercase()
        val regex = Regex("""(\d{1,2}):(\d{2})\s*(AM|PM)?""")
        val match = regex.find(trimmed) ?: return null
        var hour = match.groupValues[1].toIntOrNull() ?: return null
        val minute = match.groupValues[2].toIntOrNull() ?: return null
        val meridiem = match.groupValues[3]

        if (meridiem == "PM" && hour != 12) hour += 12
        if (meridiem == "AM" && hour == 12) hour = 0
        if (hour !in 0..23 || minute !in 0..59) return null

        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }
    }
}

class MedicineReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra(EXTRA_MEDICINE_NAME) ?: "Medicine"
        val dose = intent.getStringExtra(EXTRA_MEDICINE_DOSE) ?: ""
        val requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, 0)

        MedicineReminderScheduler.ensureChannel(context)

        val contentIntent = Intent(context, MainActivity::class.java)
        val pendingContentIntent = PendingIntent.getActivity(
            context, requestCode, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground_logo)
            .setContentTitle("Time to take $name")
            .setContentText(dose.ifBlank { "Tap to open LifeVault" })
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingContentIntent)
            .setAutoCancel(true)
            .build()

        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ) {
            NotificationManagerCompat.from(context).notify(requestCode, notification)
        }
    }
}
