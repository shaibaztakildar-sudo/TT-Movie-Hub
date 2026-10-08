package com.example.ui.screens.auth

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.ui.theme.StatusError
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authRepository: AuthRepository,
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToAdminLogin: (() -> Unit)? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // TT Logo Mark
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(BrandRed, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TT",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "TT Movie Hub",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = "Sign in to stream unlimited movies & series",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = StatusError,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
            }

            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextMuted) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = outlinedColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_email_input")
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = TextMuted
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = outlinedColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input")
            )

            // Forgot Password link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onNavigateToForgotPassword) {
                    Text("Forgot Password?", color = TextSecondary, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login Button
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Please enter both email and password"
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        val result = authRepository.login(email, password)
                        isLoading = false
                        if (result.isSuccess) {
                            onLoginSuccess()
                        } else {
                            errorMessage = result.exceptionOrNull()?.message ?: "Login failed"
                        }
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("login_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Go to Sign Up
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("New to TT Movie Hub? ", color = TextSecondary, fontSize = 14.sp)
                TextButton(
                    onClick = onNavigateToSignUp,
                    modifier = Modifier.testTag("go_to_signup_button")
                ) {
                    Text("Sign Up Now", color = BrandRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            if (onNavigateToAdminLogin != null) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onNavigateToAdminLogin,
                    modifier = Modifier.testTag("login_admin_portal_btn")
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = BrandGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Administrator? Access Admin Portal", color = BrandGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SignUpScreen(
    authRepository: AuthRepository,
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isAdminSetup by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(BrandRed, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TT",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = "Join TT Movie Hub to watch anywhere, anytime",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = StatusError,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; errorMessage = null },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted) },
                singleLine = true,
                colors = outlinedColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_name_input")
            )
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextMuted) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = outlinedColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_email_input")
            )
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = { Text("Password (min 6 characters)") },
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
                colors = outlinedColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_password_input")
            )
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; errorMessage = null },
                label = { Text("Confirm Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = outlinedColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_confirm_password_input")
            )

            // Master Admin toggle (only allowed when no admins yet exist)
            LaunchedEffect(Unit) {
                val hasAdmin = authRepository.hasAnyAdmin()
                if (!hasAdmin) {
                    isAdminSetup = true // First user automatically becomes master administrator
                }
            }

            if (isAdminSetup) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrandGold.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "★ Initial Master Admin Setup: This account will have Administrator privileges to manage movies and series.",
                        color = BrandGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (password != confirmPassword) {
                        errorMessage = "Passwords do not match"
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        val result = authRepository.signUp(name, email, password, makeAdmin = isAdminSetup)
                        isLoading = false
                        if (result.isSuccess) {
                            onSignUpSuccess()
                        } else {
                            errorMessage = result.exceptionOrNull()?.message ?: "Sign up failed"
                        }
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("signup_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account? ", color = TextSecondary, fontSize = 14.sp)
                TextButton(onClick = onNavigateToLogin) {
                    Text("Sign In", color = BrandRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun ForgotPasswordScreen(
    authRepository: AuthRepository,
    onNavigateBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Reset Password",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = "Enter your registered email and choose a new password",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            if (message != null) {
                Text(
                    text = message!!,
                    color = if (isSuccess) BrandGold else StatusError,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; message = null },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextMuted) },
                singleLine = true,
                colors = outlinedColors(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it; message = null },
                label = { Text("New Password (min 6 chars)") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = outlinedColors(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; message = null },
                label = { Text("Confirm New Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = outlinedColors(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (newPassword != confirmPassword) {
                        message = "Passwords do not match"
                        isSuccess = false
                        return@Button
                    }
                    isLoading = true
                    scope.launch {
                        val result = authRepository.resetPassword(email, newPassword)
                        isLoading = false
                        if (result.isSuccess) {
                            isSuccess = true
                            message = "Password successfully updated! You can now sign in."
                        } else {
                            isSuccess = false
                            message = result.exceptionOrNull()?.message ?: "Reset failed"
                        }
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Update Password", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateBack) {
                Text("Back to Sign In", color = TextSecondary)
            }
        }
    }
}

@Composable
private fun outlinedColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = BrandRed,
    unfocusedBorderColor = DarkBorder,
    focusedLabelColor = BrandRed,
    unfocusedLabelColor = TextMuted,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = DarkSurface,
    unfocusedContainerColor = DarkSurface
)
