package com.rudresh.xpulse.feature.patient

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudresh.xpulse.core.scanner.QrScanner
import com.rudresh.xpulse.core.security.BiometricGate
import com.rudresh.xpulse.core.domain.model.LabOrderStatus
import com.rudresh.xpulse.core.domain.model.TicketStatus
import com.rudresh.xpulse.core.domain.model.User
import com.rudresh.xpulse.ui.theme.DangerRed
import com.rudresh.xpulse.ui.theme.SuccessGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PatientApp(
    user: User,
    onLogout: () -> Unit,
    viewModel: PatientViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var tab by remember { mutableStateOf(0) }

    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.scheduleReminderNotifications()
    }

    LaunchedEffect(state.reminders.isNotEmpty()) {
        if (state.reminders.isEmpty()) return@LaunchedEffect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.scheduleReminderNotifications()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        "Hi, ${user.name.substringBefore(" ")}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "Patient",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(onClick = onLogout) { Text("Log out") }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp)) {
                when (tab) {
                    0 -> HomeTab(state, viewModel::markTaken, viewModel::markSkipped, viewModel::addWaterGlass)
                    1 -> MedicinesTab(
                        state = state,
                        onImagePicked = viewModel::onImagePicked,
                        onUpdateDraft = viewModel::updateDraft,
                        onRemoveDraft = viewModel::removeDraft,
                        onAddBlankDraft = viewModel::addBlankDraft,
                        onConfirmDraft = viewModel::confirmDraft,
                        onCancelCapture = viewModel::cancelCapture,
                    )
                    2 -> ShareTab(state, viewModel::grant, viewModel::revoke)
                    3 -> ProfileTab(
                        user = user,
                        state = state,
                        onLogout = onLogout,
                        onToggleAbha = viewModel::toggleAbha,
                        onToggleInsurance = viewModel::toggleInsurance,
                        onSetFingerprintLock = viewModel::setFingerprintLock,
                        onUpdateConditions = viewModel::updateMedicalConditions,
                        onRaiseTicket = viewModel::raiseTicket,
                        onAddReport = viewModel::addReport,
                    )
                    else -> ScanTab(
                        scanning = state.scanning,
                        checkedIn = state.checkedIn,
                        error = state.checkInError,
                        onCheckIn = viewModel::checkIn,
                        onDone = { viewModel.resetCheckIn(); tab = 0 },
                    )
                }
            }

            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text("Home") },
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Filled.Medication, contentDescription = null) },
                    label = { Text("Medicines") },
                )
                Spacer(Modifier.weight(1f))
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    icon = { Icon(Icons.Filled.Share, contentDescription = null) },
                    label = { Text("Share") },
                )
                NavigationBarItem(
                    selected = tab == 3,
                    onClick = { tab = 3; viewModel.loadAudit(); viewModel.loadTickets(); viewModel.loadRecommendations() },
                    icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    label = { Text("Profile") },
                )
            }
        }

        FloatingActionButton(
            onClick = { tab = 4 },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 30.dp)
                .size(64.dp),
        ) {
            Icon(Icons.Filled.QrCodeScanner, contentDescription = "Scan", modifier = Modifier.size(28.dp))
        }
    }
}

private val REPORT_CATEGORIES = listOf("Blood test", "X-ray", "MRI", "Prescription", "Discharge", "Other")

private val HEALTH_QUOTES = listOf(
    "Small daily habits build lifelong health.",
    "A glass of water now beats a headache later.",
    "Consistency with medicines matters more than perfection.",
    "Your body hears everything your mind says.",
    "Rest is productive too.",
)

private val DIET_SWAPS = mapOf(
    "Rice" to "Millets",
    "Wheat Roti" to "Ragi Roti",
    "Potato" to "Sweet Potato",
    "Sugar" to "Jaggery",
    "Milk" to "Almond Milk",
)

private data class Meal(val label: String, val items: List<String>)

private val DIET_PLAN = listOf(
    Meal("Breakfast", listOf("Wheat Roti", "Milk")),
    Meal("Lunch", listOf("Rice", "Potato")),
    Meal("Dinner", listOf("Rice", "Sugar")),
)

private fun timeLabel(millis: Long): String = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(millis))

@Composable
private fun CenteredSpinner() {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun EmptyState(icon: ImageVector, text: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(12.dp))
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun HomeTab(
    state: PatientState,
    onTake: (String) -> Unit,
    onSkip: (String) -> Unit,
    onAddWater: () -> Unit,
) {
    if (state.medicinesLoading && state.reminders.isEmpty()) {
        CenteredSpinner()
        return
    }

    val now = System.currentTimeMillis()
    val pending = state.reminders.filterNot { it.taken || it.skipped }
    val missed = pending.filter { it.atMillis < now }
    val upcoming = pending.filter { it.atMillis >= now }
    val takenCount = state.reminders.count { it.taken }
    val total = state.reminders.size
    val nextDose = upcoming.minByOrNull { it.atMillis } ?: missed.minByOrNull { it.atMillis }
    val context = LocalContext.current

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.FormatQuote, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(HEALTH_QUOTES[remember { HEALTH_QUOTES.indices.random() }], style = MaterialTheme.typography.bodySmall)
                }
                Surface(
                    shape = CircleShape,
                    color = DangerRed,
                    modifier = Modifier
                        .size(52.dp)
                        .clickable { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:102"))) },
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Filled.Call, contentDescription = "Emergency SOS", tint = MaterialTheme.colorScheme.onError)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (nextDose != null) {
                        Text("Next dose", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                        Spacer(Modifier.height(4.dp))
                        Text(nextDose.medicineName, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
                        Text("${nextDose.dose} · ${timeLabel(nextDose.atMillis)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                            Spacer(Modifier.width(8.dp))
                            Text("All caught up for today", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        if (total > 0) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Today's adherence", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("$takenCount of $total taken", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { if (total > 0) takenCount / total.toFloat() else 0f },
                    color = SuccessGreen,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                )
                Spacer(Modifier.height(20.dp))
            }
        }

        if (missed.isNotEmpty()) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = DangerRed, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Missed reminders", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = DangerRed)
                }
                Spacer(Modifier.height(8.dp))
            }
            items(missed) { reminder ->
                ReminderCard(reminder = reminder, missed = true, onTake = { onTake(reminder.id) }, onSkip = { onSkip(reminder.id) })
            }
            item { Spacer(Modifier.height(12.dp)) }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Upcoming today", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
        }
        if (upcoming.isEmpty()) {
            item {
                Text("Nothing else scheduled.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
            }
        } else {
            items(upcoming) { reminder ->
                ReminderCard(reminder = reminder, missed = false, onTake = { onTake(reminder.id) }, onSkip = { onSkip(reminder.id) })
            }
            item { Spacer(Modifier.height(12.dp)) }
        }

        item {
            WaterCard(glasses = state.waterGlasses, target = state.waterTarget, onAddWater = onAddWater)
        }
    }
}

@Composable
private fun ReminderCard(
    reminder: Reminder,
    missed: Boolean,
    onTake: () -> Unit,
    onSkip: () -> Unit,
) {
    val accent = if (missed) DangerRed else MaterialTheme.colorScheme.primary
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (missed) DangerRed.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (missed) 0.dp else 1.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = accent.copy(alpha = 0.15f), modifier = Modifier.size(36.dp)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(Icons.Filled.Medication, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                }
            }
            Column(modifier = Modifier.padding(start = 10.dp).weight(1f)) {
                Text(reminder.medicineName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("${reminder.dose} · ${timeLabel(reminder.atMillis)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onSkip) { Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Button(
                onClick = onTake,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                modifier = Modifier.height(36.dp),
            ) { Text(if (missed) "Take now" else "Take") }
        }
    }
}

@Composable
private fun WaterCard(glasses: Int, target: Int, onAddWater: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f), modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                }
            }
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text("Water intake", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("$glasses of $target glasses", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(
                onClick = onAddWater,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
            ) { Text("+1 Glass") }
        }
    }
}

@Composable
private fun MedicinesTab(
    state: PatientState,
    onImagePicked: (String) -> Unit,
    onUpdateDraft: (String, String?, String?, String?) -> Unit,
    onRemoveDraft: (String) -> Unit,
    onAddBlankDraft: () -> Unit,
    onConfirmDraft: () -> Unit,
    onCancelCapture: () -> Unit,
) {
    if (state.capturedImageUri != null) {
        PrescriptionCaptureFlow(
            state = state,
            onUpdateDraft = onUpdateDraft,
            onRemoveDraft = onRemoveDraft,
            onAddBlankDraft = onAddBlankDraft,
            onConfirm = onConfirmDraft,
            onCancel = onCancelCapture,
        )
        return
    }

    var section by remember { mutableStateOf(0) }

    Column(modifier = Modifier.padding(top = 20.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                .padding(4.dp),
        ) {
            SegmentButton("Medicines", section == 0, Modifier.weight(1f)) { section = 0 }
            SegmentButton("Diet", section == 1, Modifier.weight(1f)) { section = 1 }
            SegmentButton("Reports", section == 2, Modifier.weight(1f)) { section = 2 }
        }
        Spacer(Modifier.height(16.dp))

        when (section) {
            1 -> DietSection()
            2 -> ReportsSection(state)
            else -> MedicinesSection(state, onImagePicked)
        }
    }
}

@Composable
private fun ReportsSection(state: PatientState) {
    if (state.labOrdersLoading && state.labOrders.isEmpty()) {
        CenteredSpinner()
        return
    }
    if (state.labOrders.isEmpty()) {
        EmptyState(Icons.Filled.Science, "No lab tests yet.")
        return
    }
    LazyColumn {
        items(state.labOrders) { order ->
            val completed = order.status == LabOrderStatus.COMPLETED
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (completed) SuccessGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    Icons.Filled.Science,
                                    contentDescription = null,
                                    tint = if (completed) SuccessGreen else MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                            Text(order.testName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(
                                when (order.status) {
                                    LabOrderStatus.ORDERED -> "Awaiting sample collection"
                                    LabOrderStatus.SAMPLE_COLLECTED -> "Sample collected · processing"
                                    LabOrderStatus.COMPLETED -> "Report ready"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (completed) {
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            color = SuccessGreen.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                order.resultSummary.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SegmentButton(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MaterialTheme.colorScheme.surface else androidx.compose.ui.graphics.Color.Transparent,
    ) {
        Text(
            label,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        )
    }
}

@Composable
private fun DietSection() {
    LazyColumn {
        items(DIET_PLAN) { meal ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(meal.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(10.dp))
                    meal.items.forEach { item ->
                        var swapped by remember(item) { mutableStateOf(false) }
                        val alt = DIET_SWAPS[item]
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(if (swapped && alt != null) alt else item, style = MaterialTheme.typography.bodyMedium)
                            if (alt != null) {
                                TextButton(onClick = { swapped = !swapped }) {
                                    Icon(Icons.Filled.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (swapped) "Original" else "Regional swap", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicinesSection(
    state: PatientState,
    onImagePicked: (String) -> Unit,
) {
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { onImagePicked(it.toString()) }
    }

    LazyColumn {
        item {
            ScanPrescriptionCard(
                onClick = { pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            )
            Spacer(Modifier.height(16.dp))
            if (state.addedMessage != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(state.addedMessage, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(12.dp))
            }
        }
        if (state.medicinesLoading && state.medicines.isEmpty()) {
            item { CenteredSpinner() }
        } else if (state.medicines.isEmpty()) {
            item { EmptyState(Icons.Filled.Medication, "No medicines yet.") }
        } else {
            items(state.medicines) { m ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), modifier = Modifier.size(40.dp)) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(Icons.Filled.Medication, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(m.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("${m.dose} · ${m.frequency}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanPrescriptionCard(onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text("Scan a paper prescription", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("Let AI read it and add medicines automatically", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PrescriptionCaptureFlow(
    state: PatientState,
    onUpdateDraft: (String, String?, String?, String?) -> Unit,
    onRemoveDraft: (String) -> Unit,
    onAddBlankDraft: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(vertical = 20.dp)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Scan prescription", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onCancel) { Icon(Icons.Filled.Close, contentDescription = "Cancel") }
            }
            Spacer(Modifier.height(12.dp))
            state.capturedImageUri?.let { PickedImagePreview(it) }
            Spacer(Modifier.height(16.dp))
        }

        if (state.extracting) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(10.dp))
                    Text("Reading your prescription...", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "On-device text recognition. Results are a draft — always verify before saving.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        state.extractionNote ?: "Review the extracted medicines below, edit anything that's wrong, then add them.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
            items(state.draftMedicines, key = { it.id }) { draft ->
                DraftMedicineRow(
                    draft = draft,
                    onChange = { name, dose, frequency -> onUpdateDraft(draft.id, name, dose, frequency) },
                    onRemove = { onRemoveDraft(draft.id) },
                )
            }
            item {
                TextButton(onClick = onAddBlankDraft) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add another medicine")
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onConfirm,
                    enabled = state.draftMedicines.isNotEmpty() && state.draftMedicines.all { it.name.isNotBlank() },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                ) { Text("Add ${state.draftMedicines.size} medicine(s) to my list") }
            }
        }
    }
}

@Composable
private fun DraftMedicineRow(
    draft: DraftMedicine,
    onChange: (String?, String?, String?) -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Extracted item", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                IconButton(onClick = onRemove) { Icon(Icons.Filled.Close, contentDescription = "Remove", modifier = Modifier.size(18.dp)) }
            }
            OutlinedTextField(
                value = draft.name,
                onValueChange = { onChange(it, null, null) },
                label = { Text("Medicine name") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Row {
                OutlinedTextField(
                    value = draft.dose,
                    onValueChange = { onChange(null, it, null) },
                    label = { Text("Dose") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = draft.frequency,
                    onValueChange = { onChange(null, null, it) },
                    label = { Text("Frequency") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun PickedImagePreview(uriString: String) {
    val context = LocalContext.current
    var bitmap by remember(uriString) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(uriString) {
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(Uri.parse(uriString))?.use { BitmapFactory.decodeStream(it) }
            }.getOrNull()
        }
    }
    val currentBitmap = bitmap
    if (currentBitmap != null) {
        Image(
            bitmap = currentBitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)),
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun ShareTab(
    state: PatientState,
    onGrant: () -> Unit,
    onRevoke: () -> Unit,
) {
    val gid = state.grantId
    val msg = state.message
    Column(modifier = Modifier.padding(vertical = 20.dp)) {
        Text(
            "Grant Dr. Mehta temporary access to your medicines and allergies. They'll see the request instantly in their app.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onGrant,
            enabled = !state.grantBusy,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth().height(50.dp),
        ) {
            if (state.grantBusy && gid == null) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Grant access · 2 min")
            }
        }

        if (gid != null) {
            Spacer(Modifier.height(20.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreen)
                    Column(modifier = Modifier.padding(start = 10.dp)) {
                        Text("Access granted", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("Dr. Mehta can now view your data.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            if (state.grantRevoked) {
                StatusPill(text = "Revoked", background = MaterialTheme.colorScheme.error.copy(alpha = 0.12f), color = MaterialTheme.colorScheme.error)
            } else {
                OutlinedButton(
                    onClick = onRevoke,
                    enabled = !state.grantBusy,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                ) {
                    if (state.grantBusy) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("Revoke access", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        if (msg != null) {
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(msg, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ProfileTab(
    user: User,
    state: PatientState,
    onLogout: () -> Unit,
    onToggleAbha: () -> Unit,
    onToggleInsurance: () -> Unit,
    onSetFingerprintLock: (Boolean) -> Unit,
    onUpdateConditions: (Set<String>) -> Unit,
    onRaiseTicket: (String, String) -> Unit,
    onAddReport: (String, String, String) -> Unit,
) {
    val bmi = remember(state.heightCm, state.weightKg) {
        val h = state.heightCm.toFloatOrNull()?.div(100f)
        val w = state.weightKg.toFloatOrNull()
        if (h != null && h > 0f && w != null) w / (h * h) else null
    }
    val lockContext = LocalContext.current
    val biometricAvailable = remember { BiometricGate.isAvailable(lockContext) }
    var reportCategory by remember { mutableStateOf(REPORT_CATEGORIES.first()) }
    val pickReport = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { onAddReport(reportCategory, "$reportCategory report", it.toString()) }
    }

    LazyColumn(modifier = Modifier.padding(vertical = 20.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), modifier = Modifier.size(56.dp)) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    }
                }
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(user.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (state.age.isNotBlank() || state.city.isNotBlank()) {
                        Text(
                            listOf(state.age.takeIf { it.isNotBlank() }?.let { "$it yrs" }, state.city.takeIf { it.isNotBlank() })
                                .filterNotNull().joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            if (bmi != null) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "BMI %.1f · %s".format(bmi, state.heightCm + "cm, " + state.weightKg + "kg"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(14.dp),
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        item {
            ProfileConnectRow(
                icon = Icons.Filled.Shield,
                title = "Ayushman Bharat (ABDM)",
                subtitle = if (state.abhaConnected) "Linked" else "Connect your ABHA health ID",
                connected = state.abhaConnected,
                onClick = onToggleAbha,
            )
            Spacer(Modifier.height(10.dp))
            ProfileConnectRow(
                icon = Icons.Filled.VerifiedUser,
                title = "Insurance",
                subtitle = if (state.insuranceConnected) "Linked" else "Connect your insurance provider",
                connected = state.insuranceConnected,
                onClick = onToggleInsurance,
            )
            Spacer(Modifier.height(20.dp))
        }

        item {
            Text("Medical history", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Diabetes", "Hypertension", "Asthma", "Thyroid", "Heart Disease", "None").forEach { option ->
                    FilterChip(
                        selected = option in state.medicalConditions,
                        onClick = {
                            val current = state.medicalConditions
                            val next = if (option == "None") {
                                setOf("None")
                            } else if (option in current) {
                                current - option
                            } else {
                                (current - "None") + option
                            }
                            onUpdateConditions(next)
                        },
                        label = { Text(option) },
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        item {
            Text("Medical reports", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                REPORT_CATEGORIES.forEach { option ->
                    FilterChip(
                        selected = option == reportCategory,
                        onClick = { reportCategory = option },
                        label = { Text(option) },
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !state.uploadingReport) {
                        pickReport.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.uploadingReport) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                } else {
                    Icon(Icons.Filled.UploadFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Column(modifier = Modifier.padding(start = 10.dp).weight(1f)) {
                    Text("Upload $reportCategory report", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("Stored in your record and shared only with consent", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(10.dp))
        }
        items(state.reports) { report ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), modifier = Modifier.size(32.dp)) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Filled.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                }
                Column(modifier = Modifier.padding(start = 10.dp)) {
                    Text(report.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (report.stored) {
                            "${report.category} · ${report.fileBytes / 1024} KB stored"
                        } else {
                            "${report.category} · no file"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }

        item {
            Text("Access control", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
        }
        if (state.auditLoading && state.audit.isEmpty()) {
            item { CenteredSpinner() }
        } else if (state.audit.isEmpty()) {
            item {
                Text("No activity yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(20.dp))
            }
        } else {
            items(state.audit) { e ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), modifier = Modifier.size(32.dp)) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Filled.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                    }
                    Column(modifier = Modifier.padding(start = 10.dp)) {
                        Text(e.actor, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(e.action, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item { Spacer(Modifier.height(20.dp)) }
        }

        item {
            Text("Help & support", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            RaiseTicketCard(
                raising = state.raisingTicket,
                message = state.ticketMessage,
                onRaise = onRaiseTicket,
            )
            Spacer(Modifier.height(10.dp))
        }
        items(state.tickets) { ticket ->
            val resolved = ticket.status == TicketStatus.RESOLVED
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.SupportAgent,
                            contentDescription = null,
                            tint = if (resolved) SuccessGreen else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(ticket.subject, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text(
                            if (resolved) "Resolved" else "Open",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (resolved) SuccessGreen else MaterialTheme.colorScheme.primary,
                        )
                    }
                    if (resolved) {
                        Spacer(Modifier.height(8.dp))
                        Text(ticket.resolution.orEmpty(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }

        item {
            Text("Settings", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Fingerprint lock", style = MaterialTheme.typography.bodyMedium)
                        if (!biometricAvailable) {
                            Text(
                                BiometricGate.unavailableReason(lockContext),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Switch(
                    checked = state.fingerprintLockEnabled,
                    enabled = biometricAvailable,
                    onCheckedChange = { wantsOn ->
                        val host = lockContext as? FragmentActivity ?: return@Switch
                        if (!wantsOn) {
                            onSetFingerprintLock(false)
                            return@Switch
                        }
                        BiometricGate.authenticate(
                            activity = host,
                            title = "Enable app lock",
                            subtitle = "Confirm it's you",
                            onSuccess = { onSetFingerprintLock(true) },
                            onFailure = {},
                        )
                    },
                )
            }
            Spacer(Modifier.height(20.dp))
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Care suggestions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Generated from your conditions, BMI, medicines and lab results. Not a diagnosis.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            if (state.recommendationsLoading && state.recommendations.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
            }
        }
        items(state.recommendations) { rec ->
            val accent = when (rec.urgency) {
                "ACTION" -> MaterialTheme.colorScheme.error
                "ROUTINE" -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(rec.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Surface(color = accent.copy(alpha = 0.15f), shape = RoundedCornerShape(50)) {
                            Text(
                                rec.specialty,
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(rec.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Spacer(Modifier.height(24.dp))
            OutlinedButton(
                onClick = onLogout,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp),
            ) { Text("Log out", color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun RaiseTicketCard(
    raising: Boolean,
    message: String?,
    onRaise: (String, String) -> Unit,
) {
    var subject by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.SupportAgent, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Contact support", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = detail,
                onValueChange = { detail = it },
                label = { Text("What went wrong?") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    onRaise(subject, detail)
                    subject = ""
                    detail = ""
                },
                enabled = subject.isNotBlank() && detail.isNotBlank() && !raising,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (raising) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Raise ticket")
                }
            }
            if (message != null) {
                Spacer(Modifier.height(10.dp))
                Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ProfileConnectRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    connected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = if (connected) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant)
        Column(modifier = Modifier.padding(start = 10.dp).weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (connected) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreen)
        } else {
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatusPill(text: String, background: androidx.compose.ui.graphics.Color, color: androidx.compose.ui.graphics.Color) {
    Surface(color = background, shape = RoundedCornerShape(50)) {
        Text(
            text,
            color = color,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun ScanTab(
    scanning: Boolean,
    checkedIn: Boolean,
    error: String?,
    onCheckIn: (String) -> Unit,
    onDone: () -> Unit,
) {
    var code by remember { mutableStateOf("") }
    var scanError by remember { mutableStateOf<String?>(null) }
    val scanContext = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            if (checkedIn) "You're checked in" else "Check in at reception",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            if (checkedIn) {
                "The front desk has been notified."
            } else {
                "Enter the 6-digit code shown on the reception screen."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f), RoundedCornerShape(20.dp))
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center,
        ) {
            when {
                checkedIn -> Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(72.dp))
                scanning -> ScanLine()
                else -> Icon(
                    Icons.Filled.QrCodeScanner,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(72.dp),
                )
            }
        }
        Spacer(Modifier.height(24.dp))

        if (checkedIn) {
            Button(
                onClick = onDone,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth().height(50.dp),
            ) { Text("Done") }
            return@Column
        }

        Button(
            onClick = {
                scanError = null
                QrScanner.scan(
                    context = scanContext,
                    onResult = { value ->
                        code = value.filter { it.isDigit() }.take(6)
                        onCheckIn(code)
                    },
                    onError = { scanError = it },
                )
            },
            enabled = !scanning,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            if (scanning) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Scan reception QR")
            }
        }

        Spacer(Modifier.height(18.dp))
        Text(
            "or enter the code manually",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = code,
            onValueChange = { input -> code = input.filter { it.isDigit() }.take(6) },
            label = { Text("Reception code") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = { onCheckIn(code) },
            enabled = !scanning && code.length == 6,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) { Text("Check in with code") }

        val shownError = error ?: scanError
        if (shownError != null) {
            Spacer(Modifier.height(14.dp))
            Text(shownError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ScanLine() {
    val transition = rememberInfiniteTransition(label = "scan")
    val offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1100, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "scanLine",
    )
    Canvas(modifier = Modifier.size(220.dp)) {
        val y = size.height * offset
        drawLine(
            color = SuccessGreen,
            start = Offset(12f, y),
            end = Offset(size.width - 12f, y),
            strokeWidth = 6f,
        )
    }
}

