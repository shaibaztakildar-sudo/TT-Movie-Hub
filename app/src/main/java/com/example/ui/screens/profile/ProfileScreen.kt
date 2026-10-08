package com.example.ui.screens.profile

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.entity.UserEntity
import com.example.data.repository.AuthRepository
import com.example.ui.components.resolveImageModel
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

@Composable
fun ProfileScreen(
    currentUser: UserEntity?,
    authRepository: AuthRepository,
    onNavigateToMyList: () -> Unit,
    onNavigateToDownloads: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToAdminLogin: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "My Profile",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        )
        Spacer(modifier = Modifier.height(20.dp))

        if (currentUser == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(DarkSurface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Sign in to TT Movie Hub",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Access your personal watchlist, downloads, and continue watching features.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                    )
                    Button(
                        onClick = onNavigateToLogin,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("profile_login_button")
                    ) {
                        Text("Sign In / Register", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(
                        onClick = onNavigateToAdminLogin,
                        modifier = Modifier.testTag("unauth_admin_portal_btn")
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = BrandGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Administrator Portal", color = BrandGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Logged-in User Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Photo / Avatar
                    val photoModel = resolveImageModel(currentUser.profilePhotoUri)
                    if (photoModel != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(photoModel)
                                .crossfade(true)
                                .build(),
                            contentDescription = currentUser.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(BrandRed, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser.name.take(1).uppercase(),
                                color = TextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 26.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = currentUser.email,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (currentUser.role == "ADMIN") BrandGold else DarkBorder,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (currentUser.role == "ADMIN") "★ ADMINISTRATOR" else "MEMBER",
                                    color = if (currentUser.role == "ADMIN") DarkBackground else TextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Admin Hub Section (PROTECTED: ONLY visible and accessible to role == "ADMIN")
            if (currentUser.role == "ADMIN") {
                Text(
                    text = "Administrator Management",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = BrandGold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToAdmin)
                        .testTag("admin_portal_card"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(BrandGold.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = BrandGold)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Admin Control Hub",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Add & edit movies, web series, episodes, genres and uploads",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Quick App Actions
            Text(
                text = "My Content",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column {
                    ProfileMenuItem(
                        icon = Icons.Default.Bookmark,
                        title = "My Watchlist",
                        subtitle = "Saved movies & series",
                        onClick = onNavigateToMyList
                    )
                    ProfileMenuItem(
                        icon = Icons.Default.Download,
                        title = "Offline Downloads",
                        subtitle = "Downloaded videos ready to watch",
                        onClick = onNavigateToDownloads
                    )
                    ProfileMenuItem(
                        icon = Icons.Default.AdminPanelSettings,
                        title = "Administrator Portal",
                        subtitle = if (currentUser.role == "ADMIN") "Open Admin Control Panel" else "Sign in as administrator",
                        onClick = {
                            if (currentUser.role == "ADMIN") onNavigateToAdmin() else onNavigateToAdminLogin()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Log Out Button
            Button(
                onClick = {
                    authRepository.logout()
                    onLogout()
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("logout_button")
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = StatusError)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", color = StatusError, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "TT Movie Hub • Version 1.0.0",
                color = TextMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = BrandRed, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
            Text(text = subtitle, color = TextMuted, fontSize = 11.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
    }
}
