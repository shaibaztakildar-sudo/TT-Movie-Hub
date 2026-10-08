package com.example.ui.screens.admin

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AuthRepository
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BrandRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.StatusError
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun AdminLoginScreen(
    authRepository: AuthRepository,
    onAdminLoginSuccess: () -> Unit,
    onBackToUserApp: () -> Unit
) {
    var identifier by remember { mutableStateOf("") } // email or phone
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var hasAdminInDb by remember { mutableStateOf(true) }
    var showSetupInitialAdmin by remember { mutableStateOf(false) }

    // Initial admin registration fields if database has 0 admins
    var setupName by remember { mutableStateOf("") }
    var setupEmail by remember { mutableStateOf("") }
    var setupPhone by remember { mutableStateOf("") }
    var setupPassword by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val hasAdmin = authRepository.hasAnyAdmin()
        hasAdminInDb = hasAdmin
        if (!hasAdmin) {
            showSetupInitialAdmin = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header with Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToUserApp,
                    modifier = Modifier.testTag("admin_login_back_button")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Return to User App",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dedicated Admin Shield Emblem
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(BrandGold.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = "Admin Security Portal",
                    tint = BrandGold,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "TT MOVIE HUB",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = BrandRed,
                    letterSpacing = 2.sp
                )
            )

            Text(
                text = if (showSetupInitialAdmin) "Admin Portal Setup" else "Admin Authentication",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            )

            Text(
                text = if (showSetupInitialAdmin) {
                    "No administrator account is configured yet. Set up the master administrator credentials to access the control panel."
                } else {
                    "Protected area restricted strictly to authorized administrators."
                },
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
            )

            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusError.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = StatusError, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage!!,
                            color = StatusError,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (showSetupInitialAdmin) {
                // Initial Admin Setup Mode
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Configure Primary Admin",
                            fontWeight = FontWeight.Bold,
                            color = BrandGold,
                            fontSize = 15.sp
                        )

                        OutlinedTextField(
                            value = setupName,
                            onValueChange = { setupName = it; errorMessage = null },
                            label = { Text("Admin Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted) },
                            singleLine = true,
                            colors = adminLoginFieldColors(),
                            modifier = Modifier.fillMaxWidth().testTag("setup_admin_name")
                        )

                        OutlinedTextField(
                            value = setupEmail,
                            onValueChange = { setupEmail = it; errorMessage = null },
                            label = { Text("Admin Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextMuted) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = adminLoginFieldColors(),
                            modifier = Modifier.fillMaxWidth().testTag("setup_admin_email")
                        )

                        OutlinedTextField(
                            value = setupPhone,
                            onValueChange = { setupPhone = it },
                            label = { Text("Admin Mobile (Optional)") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TextMuted) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = adminLoginFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = setupPassword,
                            onValueChange = { setupPassword = it; errorMessage = null },
                            label = { Text("Admin Secure Password (min 6 chars)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = adminLoginFieldColors(),
                            modifier = Modifier.fillMaxWidth().testTag("setup_admin_password")
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                if (setupName.isBlank() || setupEmail.isBlank() || setupPassword.length < 6) {
                                    errorMessage = "Please enter name, email, and a password of at least 6 characters"
                                    return@Button
                                }
                                isLoading = true
                                errorMessage = null
                                scope.launch {
                                    val res = authRepository.createInitialAdmin(
                                        name = setupName.trim(),
                                        email = setupEmail.trim(),
                                        phone = setupPhone.trim(),
                                        password = setupPassword
                                    )
                                    isLoading = false
                                    if (res.isSuccess) {
                                        onAdminLoginSuccess()
                                    } else {
                                        errorMessage = res.exceptionOrNull()?.message ?: "Setup failed"
                                    }
                                }
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("setup_admin_submit_btn")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Create & Launch Admin Panel", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        // Quick prefill for owner
                        OutlinedButton(
                            onClick = {
                                setupName = "TT Movie Hub Admin"
                                setupEmail = "admin@ttmoviehub.com"
                                setupPassword = "Admin@123"
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Use Default Admin Credentials (admin@ttmoviehub.com)", fontSize = 11.sp, color = BrandGold)
                        }
                    }
                }
            } else {
                // Standard Protected Admin Login
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = identifier,
                            onValueChange = { identifier = it; errorMessage = null },
                            label = { Text("Admin Email or Mobile") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextMuted) },
                            singleLine = true,
                            colors = adminLoginFieldColors(),
                            modifier = Modifier.fillMaxWidth().testTag("admin_login_identifier")
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorMessage = null },
                            label = { Text("Admin Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = TextMuted
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = adminLoginFieldColors(),
                            modifier = Modifier.fillMaxWidth().testTag("admin_login_password")
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                if (identifier.isBlank() || password.isBlank()) {
                                    errorMessage = "Please enter both admin email/mobile and password"
                                    return@Button
                                }
                                isLoading = true
                                errorMessage = null
                                scope.launch {
                                    val res = authRepository.loginAdmin(identifier, password)
                                    isLoading = false
                                    if (res.isSuccess) {
                                        onAdminLoginSuccess()
                                    } else {
                                        errorMessage = res.exceptionOrNull()?.message ?: "Admin authentication failed"
                                    }
                                }
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("admin_login_submit_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Authenticate as Admin", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Toggle for setup if needed
                TextButton(onClick = { showSetupInitialAdmin = true }) {
                    Text("Configure / Re-register Administrator", color = TextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun adminLoginFieldColors() = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
    focusedBorderColor = BrandGold,
    unfocusedBorderColor = DarkBorder,
    focusedLabelColor = BrandGold,
    unfocusedLabelColor = TextMuted,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = DarkSurface,
    unfocusedContainerColor = DarkSurface
)
