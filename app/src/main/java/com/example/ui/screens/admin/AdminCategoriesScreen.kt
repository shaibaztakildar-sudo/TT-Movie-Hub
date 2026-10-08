package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.UserDataRepository
import com.example.ui.theme.BrandRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.StatusError
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun AdminCategoriesScreen(
    userDataRepository: UserDataRepository,
    onBack: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val genres by userDataRepository.allGenres.collectAsState(initial = emptyList())
    val languages by userDataRepository.allLanguages.collectAsState(initial = emptyList())

    var newGenreName by remember { mutableStateOf("") }
    var newLanguageName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Manage Genres & Languages",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Genres Section
            item {
                Text(
                    text = "Custom Genres (${genres.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newGenreName,
                        onValueChange = { newGenreName = it },
                        placeholder = { Text("Add Genre (e.g. Sci-Fi, Horror)") },
                        singleLine = true,
                        colors = adminTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (newGenreName.isNotBlank()) {
                                scope.launch {
                                    userDataRepository.addGenre(newGenreName)
                                    newGenreName = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add")
                    }
                }
            }

            items(genres, key = { it.id }) { g ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = g.name, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = { scope.launch { userDataRepository.deleteGenre(g.id) } }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusError)
                        }
                    }
                }
            }

            // Languages Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Supported Languages (${languages.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newLanguageName,
                        onValueChange = { newLanguageName = it },
                        placeholder = { Text("Add Language (e.g. Tamil, English)") },
                        singleLine = true,
                        colors = adminTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (newLanguageName.isNotBlank()) {
                                scope.launch {
                                    userDataRepository.addLanguage(newLanguageName)
                                    newLanguageName = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add")
                    }
                }
            }

            items(languages, key = { it.id }) { l ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = l.name, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = { scope.launch { userDataRepository.deleteLanguage(l.id) } }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusError)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCategoriesView(userDataRepository: UserDataRepository) {
    AdminCategoriesScreen(userDataRepository = userDataRepository, onBack = {})
}
