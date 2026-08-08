package com.rudresh.xpulse

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudresh.xpulse.core.domain.model.Role
import com.rudresh.xpulse.core.security.LockScreen
import com.rudresh.xpulse.feature.auth.LoginScreen
import com.rudresh.xpulse.feature.admin.AdminApp
import com.rudresh.xpulse.feature.care.CustomerCareApp
import com.rudresh.xpulse.feature.diagnostic.DiagnosticApp
import com.rudresh.xpulse.feature.doctor.DoctorApp
import com.rudresh.xpulse.feature.home.HomeScreen
import com.rudresh.xpulse.feature.patient.OnboardingScreen
import com.rudresh.xpulse.feature.patient.PatientApp
import com.rudresh.xpulse.feature.pharmacy.PharmacyApp
import com.rudresh.xpulse.feature.reception.ReceptionApp
import com.rudresh.xpulse.ui.theme.XpulseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XpulseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Root()
                }
            }
        }
    }
}

@Composable
fun Root(viewModel: MainViewModel = hiltViewModel()) {
    val user by viewModel.currentUser.collectAsState()
    val needsOnboarding by viewModel.needsOnboarding.collectAsState()
    val lockEnabled by viewModel.lockEnabled.collectAsState()
    val unlocked by viewModel.unlocked.collectAsState()
    val current = user
    if (current != null && lockEnabled && !unlocked) {
        LockScreen(onUnlocked = viewModel::markUnlocked)
    } else if (current == null) {
        LoginScreen()
    } else if (Role.PATIENT in current.roles && needsOnboarding) {
        OnboardingScreen()
    } else {
        when {
            Role.PATIENT in current.roles -> PatientApp(user = current, onLogout = viewModel::logout)
            Role.DOCTOR in current.roles -> DoctorApp(user = current, onLogout = viewModel::logout)
            Role.PHARMACY in current.roles -> PharmacyApp(user = current, onLogout = viewModel::logout)
            Role.RECEPTIONIST in current.roles -> ReceptionApp(user = current, onLogout = viewModel::logout)
            Role.DIAGNOSTIC in current.roles -> DiagnosticApp(user = current, onLogout = viewModel::logout)
            Role.CUSTOMER_CARE in current.roles -> CustomerCareApp(user = current, onLogout = viewModel::logout)
            Role.ADMIN in current.roles || Role.SUPER_ADMIN in current.roles ->
                AdminApp(user = current, onLogout = viewModel::logout)
            else -> HomeScreen(user = current, onLogout = viewModel::logout)
        }
    }
}
