package edu.project.redstream.ui.shared

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// "2 hours ago", "just now", "3 days ago"
fun Timestamp.toRelativeTime(): String {
    val now  = System.currentTimeMillis()
    val diff = now - this.toDate().time
    return when {
        diff < 60_000               -> "just now"
        diff < 3_600_000            -> "${diff / 60_000}m ago"
        diff < 86_400_000           -> "${diff / 3_600_000}h ago"
        diff < 7 * 86_400_000L      -> "${diff / 86_400_000}d ago"
        else                        -> SimpleDateFormat(
            "dd MMM", Locale.getDefault()).format(this.toDate())
    }
}

// Countdown: "4h 23m 10s" or "Expired"
fun Timestamp.toCountdown(): String {
    val remaining = this.toDate().time - System.currentTimeMillis()
    if (remaining <= 0) return "Expired"
    val h = TimeUnit.MILLISECONDS.toHours(remaining)
    val m = TimeUnit.MILLISECONDS.toMinutes(remaining) % 60
    val s = TimeUnit.MILLISECONDS.toSeconds(remaining) % 60
    return when {
        h > 0  -> "${h}h ${m}m ${s}s"
        m > 0  -> "${m}m ${s}s"
        else   -> "${s}s"
    }
}

// "Expires in 4h 23m" — shorter version for cards
fun Timestamp.toExpiryLabel(): String {
    val remaining = this.toDate().time - System.currentTimeMillis()
    if (remaining <= 0) return "Expired"
    val h = TimeUnit.MILLISECONDS.toHours(remaining)
    val m = TimeUnit.MILLISECONDS.toMinutes(remaining) % 60
    return if (h > 0) "Expires in ${h}h ${m}m" else "Expires in ${m}m"
}