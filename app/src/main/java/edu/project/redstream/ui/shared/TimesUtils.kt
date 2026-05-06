package edu.project.redstream.ui.shared

import androidx.compose.runtime.*
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// "2 hours ago", "just now", "3 days ago"
fun Timestamp.toRelativeTime(): String {
    val now  = System.currentTimeMillis()
    val diff = now - this.toDate().time
    return when {
        diff < 60_000          -> "just now"
        diff < 3_600_000       -> "${diff / 60_000}m ago"
        diff < 86_400_000      -> "${diff / 3_600_000}h ago"
        diff < 7 * 86_400_000L -> "${diff / 86_400_000}d ago"
        else                   -> SimpleDateFormat(
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
        h > 0 -> "${h}h ${m}m ${s}s"
        m > 0 -> "${m}m ${s}s"
        else  -> "${s}s"
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

// ── Live countdown — ticks every second inside a Composable ──────────────────
// Usage: val countdown by rememberCountdown(timestamp)
//        Text(countdown)  // updates every second automatically
@Composable
fun rememberCountdown(timestamp: Timestamp): State<String> {
    val countdownState = remember { mutableStateOf(timestamp.toCountdown()) }
    LaunchedEffect(timestamp) {
        while (true) {
            countdownState.value = timestamp.toCountdown()
            if (countdownState.value == "Expired") break
            delay(1_000L)
        }
    }
    return countdownState
}

// ── Live expiry label — ticks every minute inside a Composable ───────────────
// Usage: val expiry by rememberExpiryLabel(timestamp)
@Composable
fun rememberExpiryLabel(timestamp: Timestamp): State<String> {
    val expiryState = remember { mutableStateOf(timestamp.toExpiryLabel()) }
    LaunchedEffect(timestamp) {
        while (true) {
            expiryState.value = timestamp.toExpiryLabel()
            if (expiryState.value == "Expired") break
            delay(60_000L) // update every minute — sufficient for h/m display
        }
    }
    return expiryState
}