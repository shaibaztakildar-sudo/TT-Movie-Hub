package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserEntity
import com.example.ui.navigation.Screen
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BrandRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TTMovieHubTopBar(
    currentUser: UserEntity?,
    onProfileClick: () -> Unit,
    onAdminClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkBackground.copy(alpha = 0.95f))
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // TT Movie Hub Brand Mark
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(BrandRed, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "TT",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                letterSpacing = (-0.5).sp
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Movie Hub",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 18.sp,
                letterSpacing = 0.5.sp
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        // Admin Portal access button
        if (onAdminClick != null) {
            IconButton(
                onClick = onAdminClick,
                modifier = Modifier
                    .testTag("admin_panel_button")
                    .padding(end = 6.dp)
            ) {
                if (currentUser?.role == "ADMIN") {
                    BadgedBox(
                        badge = {
                            Badge(containerColor = BrandGold) {
                                Text("ADMIN", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Hub",
                            tint = BrandGold,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin Portal",
                        tint = TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Profile Avatar Button
        IconButton(
            onClick = onProfileClick,
            modifier = Modifier
                .testTag("profile_button")
                .size(36.dp)
                .background(DarkSurface, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = if (currentUser != null) TextPrimary else TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun TTMovieHubBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        NavigationItem(
            route = Screen.Home.route,
            label = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            tag = "tab_home"
        ),
        NavigationItem(
            route = Screen.Movies.route,
            label = "Movies",
            selectedIcon = Icons.Filled.Movie,
            unselectedIcon = Icons.Outlined.Movie,
            tag = "tab_movies"
        ),
        NavigationItem(
            route = Screen.Series.route,
            label = "Series",
            selectedIcon = Icons.Filled.Tv,
            unselectedIcon = Icons.Outlined.Tv,
            tag = "tab_series"
        ),
        NavigationItem(
            route = Screen.Search.route,
            label = "Search",
            selectedIcon = Icons.Filled.Search,
            unselectedIcon = Icons.Filled.Search,
            tag = "tab_search"
        ),
        NavigationItem(
            route = Screen.MyList.route,
            label = "My List",
            selectedIcon = Icons.Filled.Bookmark,
            unselectedIcon = Icons.Filled.BookmarkBorder,
            tag = "tab_my_list"
        ),
        NavigationItem(
            route = Screen.Downloads.route,
            label = "Downloads",
            selectedIcon = Icons.Filled.Download,
            unselectedIcon = Icons.Filled.FileDownload,
            tag = "tab_downloads"
        )
    )

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        containerColor = DarkSurface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrandRed,
                    selectedTextColor = BrandRed,
                    indicatorColor = BrandRed.copy(alpha = 0.15f),
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                ),
                modifier = Modifier.testTag(item.tag)
            )
        }
    }
}

private data class NavigationItem(
    val route: String,
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val tag: String
)
