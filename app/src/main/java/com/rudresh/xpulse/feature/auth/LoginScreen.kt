package com.rudresh.xpulse.feature.auth

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudresh.xpulse.ui.theme.Seashell
import com.rudresh.xpulse.ui.theme.SpaceIndigo
import com.rudresh.xpulse.ui.theme.SuccessGreen

@Composable
fun LoginScreen(viewModel: LoginViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val resetState by viewModel.resetState.collectAsState()
    val connection by viewModel.connection.collectAsState()
    val context = LocalContext.current
    var mode by remember { mutableStateOf(0) }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("patient@xpulse.in") }
    var password by remember { mutableStateOf("password") }
    var confirmPassword by remember { mutableStateOf("") }
    var acceptedTerms by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(mode) { viewModel.clearMessage() }

    Column(modifier = Modifier.fillMaxSize().background(SpaceIndigo)) {
        Column(
            modifier = Modifier.weight(0.28f).fillMaxWidth().padding(28.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Text("XPulse", style = MaterialTheme.typography.headlineSmall, color = Seashell.copy(alpha = 0.6f))
            Spacer(Modifier.height(8.dp))
            Text(
                when {
                    resetState.active -> "Reset password"
                    mode == 0 -> "Welcome back"
                    else -> "Create your account"
                },
                style = MaterialTheme.typography.headlineMedium,
                color = Seashell,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Manage your health, securely.",
                style = MaterialTheme.typography.bodyMedium,
                color = Seashell.copy(alpha = 0.75f),
            )
        }

        Surface(
            modifier = Modifier.weight(0.72f).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            if (resetState.active) {
                ResetPasswordFlow(
                    state = resetState,
                    onEmailChange = viewModel::updateResetEmail,
                    onRequestCode = viewModel::requestCode,
                    onSubmit = viewModel::submitNewPassword,
                    onCancel = viewModel::cancelReset,
                )
                return@Surface
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(28.dp),
            ) {
                ServerStatusBar(
                    connection = connection,
                    onRetry = viewModel::checkConnection,
                    onToggleEditor = viewModel::toggleServerEditor,
                    onSave = viewModel::updateServerUrl,
                )
                Spacer(Modifier.height(16.dp))

                TabRow(
                    selectedTabIndex = mode,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                ) {
                    Tab(selected = mode == 0, onClick = { mode = 0 }, text = { Text("Login") })
                    Tab(selected = mode == 1, onClick = { mode = 1 }, text = { Text("Register") })
                }
                Spacer(Modifier.height(20.dp))

                if (mode == 1) {
                    AuthField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "Full name",
                        icon = Icons.Filled.Person,
                    )
                    Spacer(Modifier.height(12.dp))
                    AuthField(
                        value = phone,
                        onValueChange = { input -> phone = input.filter { it.isDigit() }.take(10) },
                        placeholder = "10-digit mobile number",
                        icon = Icons.Filled.Phone,
                        keyboardType = KeyboardType.Phone,
                    )
                    Spacer(Modifier.height(12.dp))
                }

                AuthField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "you@example.com",
                    icon = Icons.Filled.Email,
                    keyboardType = KeyboardType.Email,
                )
                Spacer(Modifier.height(12.dp))

                AuthField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = if (mode == 0) "Password" else "Password · min 8 chars, 1 number",
                    icon = Icons.Filled.Lock,
                    isPassword = true,
                    revealed = showPassword,
                    onToggleReveal = { showPassword = !showPassword },
                )

                if (mode == 1) {
                    Spacer(Modifier.height(12.dp))
                    AuthField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = "Confirm password",
                        icon = Icons.Filled.Lock,
                        isPassword = true,
                        revealed = showPassword,
                        onToggleReveal = { showPassword = !showPassword },
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = acceptedTerms, onCheckedChange = { acceptedTerms = it })
                        Text(
                            "I agree to share my health data as per ABDM consent rules.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                } else {
                    Spacer(Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { viewModel.startReset(email) }) {
                            Text(
                                "Forgot password?",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        if (mode == 0) {
                            viewModel.login(email, password)
                        } else {
                            viewModel.register(name, email, phone, password, confirmPassword, acceptedTerms)
                        }
                    },
                    enabled = uiState != LoginUiState.Loading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    if (uiState == LoginUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(if (mode == 0) "Login" else "Create account", fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        "  or  ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show() },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) { Text("Continue with Google") }

                MessageBlock(uiState)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ServerStatusBar(
    connection: ConnectionState,
    onRetry: () -> Unit,
    onToggleEditor: () -> Unit,
    onSave: (String) -> Unit,
) {
    var draft by remember(connection.baseUrl) { mutableStateOf(connection.baseUrl) }

    val accent = when (connection.status) {
        ServerStatus.ONLINE -> SuccessGreen
        ServerStatus.OFFLINE -> MaterialTheme.colorScheme.error
        ServerStatus.CHECKING -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val label = when (connection.status) {
        ServerStatus.ONLINE -> "Server connected"
        ServerStatus.OFFLINE -> "Cannot reach server"
        ServerStatus.CHECKING -> "Waking server..."
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(accent.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (connection.status == ServerStatus.CHECKING) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = accent)
            } else {
                Box(modifier = Modifier.size(10.dp).background(accent, CircleShape))
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelLarge, color = accent, fontWeight = FontWeight.SemiBold)
                Text(
                    connection.baseUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onRetry) {
                Icon(Icons.Filled.Refresh, contentDescription = "Retry", tint = accent, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onToggleEditor) {
                Icon(Icons.Filled.Settings, contentDescription = "Change server", tint = accent, modifier = Modifier.size(18.dp))
            }
        }

        if (connection.status == ServerStatus.OFFLINE && !connection.editing) {
            Spacer(Modifier.height(6.dp))
            Text(
                "Check your internet, then tap retry. A sleeping server can take a minute to wake.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (connection.editing) {
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                label = { Text("Server address") },
                placeholder = { Text("http://192.168.0.7:3000") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { onSave(draft) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save and reconnect") }
        }
    }
}

@Composable
private fun MessageBlock(uiState: LoginUiState) {
    if (uiState is LoginUiState.Error) {
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                uiState.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
    if (uiState is LoginUiState.Info) {
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                uiState.message,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ResetPasswordFlow(
    state: ResetState,
    onEmailChange: (String) -> Unit,
    onRequestCode: () -> Unit,
    onSubmit: (String, String, String) -> Unit,
    onCancel: () -> Unit,
) {
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var revealed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(28.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onCancel) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to login")
            }
            Text(
                if (state.step == 0) "Step 1 of 2 · Verify email" else "Step 2 of 2 · New password",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(16.dp))

        if (state.step == 0) {
            Text(
                "Enter the email on your account and we'll send a 6-digit reset code.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            AuthField(
                value = state.email,
                onValueChange = onEmailChange,
                placeholder = "you@example.com",
                icon = Icons.Filled.Email,
                keyboardType = KeyboardType.Email,
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onRequestCode,
                enabled = !state.busy,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                if (state.busy) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Send reset code", fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            Text(
                "We sent a 6-digit code to ${state.email}.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (state.sentCode != null) {
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Demo build · your code is ${state.sentCode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            AuthField(
                value = otp,
                onValueChange = { input -> otp = input.filter { it.isDigit() }.take(6) },
                placeholder = "6-digit code",
                icon = Icons.Filled.Lock,
                keyboardType = KeyboardType.Number,
            )
            Spacer(Modifier.height(12.dp))
            AuthField(
                value = newPassword,
                onValueChange = { newPassword = it },
                placeholder = "New password · min 8 chars, 1 number",
                icon = Icons.Filled.Lock,
                isPassword = true,
                revealed = revealed,
                onToggleReveal = { revealed = !revealed },
            )
            Spacer(Modifier.height(12.dp))
            AuthField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "Confirm new password",
                icon = Icons.Filled.Lock,
                isPassword = true,
                revealed = revealed,
                onToggleReveal = { revealed = !revealed },
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { onSubmit(otp, newPassword, confirmPassword) },
                enabled = !state.busy,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                if (state.busy) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Update password", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (state.error != null) {
            Spacer(Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(state.error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    revealed: Boolean = false,
    onToggleReveal: (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        trailingIcon = if (isPassword && onToggleReveal != null) {
            {
                IconButton(onClick = onToggleReveal) {
                    Icon(
                        if (revealed) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (revealed) "Hide password" else "Show password",
                    )
                }
            }
        } else {
            null
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword && !revealed) PasswordVisualTransformation() else VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier.fillMaxWidth(),
    )
}
