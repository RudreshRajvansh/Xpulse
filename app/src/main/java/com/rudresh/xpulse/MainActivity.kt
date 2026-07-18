package com.rudresh.xpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudresh.xpulse.core.domain.model.Role
import com.rudresh.xpulse.feature.auth.LoginScreen
import com.rudresh.xpulse.feature.doctor.DoctorApp
import com.rudresh.xpulse.feature.home.HomeScreen
import com.rudresh.xpulse.feature.patient.PatientApp
import com.rudresh.xpulse.ui.theme.XpulseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
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
    val current = user
    if (current == null) {
        LoginScreen()
    } else {
        when {
            Role.PATIENT in current.roles -> PatientApp(user = current, onLogout = viewModel::logout)
            Role.DOCTOR in current.roles -> DoctorApp(user = current, onLogout = viewModel::logout)
            else -> HomeScreen(user = current, onLogout = viewModel::logout)
        }
    }
}
