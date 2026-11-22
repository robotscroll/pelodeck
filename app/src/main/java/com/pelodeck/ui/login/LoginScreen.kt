package com.pelodeck.ui.login

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pelodeck.data.local.entity.RiderProfile
import com.pelodeck.data.repository.RideStatus
import com.pelodeck.domain.model.ThemeSkin
import com.pelodeck.ui.profiles.ProfileSwitchConflict
import com.pelodeck.ui.profiles.ProfileViewModel
import com.pelodeck.ui.profiles.CreateProfileDialog
import com.pelodeck.ui.dashboard.components.LauncherButtons
import com.pelodeck.ui.theme.PeloDeckTheme

@Composable
fun LoginScreen(
    onNavigateHome: () -> Unit,
    onStartRideWithProfile: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val profiles by viewModel.allProfiles.collectAsState()
    val activeProfileId by viewModel.activeProfileId.collectAsState()
    val activeRide by viewModel.activeRide.collectAsState()
    val conflictState by viewModel.conflictState.collectAsState()

    val colors = PeloDeckTheme.colors
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<RiderProfile?>(null) }
    editingProfile?.let { profile ->
        CreateProfileDialog(initial = profile, onDismiss = { editingProfile = null },
            onConfirm = { name, weight, image, skin, units ->
                viewModel.updateProfile(profile, name, weight, image, skin, units)
                editingProfile = null
            })
    }

    // Conflict Dialog when an active ride is in progress for another rider
    conflictState?.let { conflict ->
        when (conflict) {
            is ProfileSwitchConflict.ActiveRideBlock -> {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissConflict() },
                    title = {
                        Text(
                            text = "ACTIVE RIDE IN PROGRESS",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = colors.secondary
                        )
                    },
                    text = {
                        Text(
                            text = "A ride is currently underway. Starting a ride for ${conflict.targetProfile.name} requires finishing or discarding the existing session.",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = colors.textPrimary
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.resolveConflictAndFinishRide(conflict.targetProfile) {
                                    onStartRideWithProfile()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.Black)
                        ) {
                            Text("FINISH PREVIOUS & START")
                        }
                    },
                    dismissButton = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.resolveConflictAndDiscardRide(conflict.targetProfile) {
                                        onStartRideWithProfile()
                                    }
                                }
                            ) {
                                Text("DISCARD PREVIOUS", color = colors.secondary)
                            }
                            TextButton(onClick = { viewModel.dismissConflict() }) {
                                Text("CANCEL", color = colors.textSecondary)
                            }
                        }
                    },
                    containerColor = colors.surface
                )
            }
        }
    }

    if (showCreateDialog) {
        CreateProfileDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, weight, img, skin, units ->
                viewModel.createProfile(name, weight, img, skin, units)
                showCreateDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Top Brand & Utility Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onNavigateHome) { Text("HOME", color = colors.primary) }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "PELODECK",
                        color = colors.primary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "OPEN CYCLING DASHBOARD // RIDER LOGIN",
                        color = colors.textSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LauncherButtons()
                OutlinedButton(
                    onClick = onNavigateToHistory,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary)
                ) {
                    Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("HISTORY", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onNavigateToDiagnostics,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.secondary)
                ) {
                    Icon(imageVector = Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("DIAGNOSTICS", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }

                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.buttonBackground,
                        contentColor = colors.primary
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ADD RIDER", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                IconButton(
                    onClick = { viewModel.reloadProfilesFromJson() },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload Profiles JSON",
                        tint = colors.accent
                    )
                }
            }
        }

        // Active Ride Banner if a ride is already in progress or paused
        val isRideActive = activeRide.status == RideStatus.IN_PROGRESS || activeRide.status == RideStatus.PAUSED
        if (isRideActive) {
            val riderName = profiles.find { it.id == activeProfileId }?.name ?: "Current Rider"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.primary, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(colors.primary)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ACTIVE RIDE IN PROGRESS: $riderName",
                                color = colors.primary,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            val minutes = activeRide.elapsedSeconds / 60
                            val seconds = activeRide.elapsedSeconds % 60
                            Text(
                                text = "Elapsed: %02d:%02d · Distance: %.2f mi · Est. Calories: %.0f kcal"
                                    .format(minutes, seconds, activeRide.distanceMiles, activeRide.estimatedCaloriesKcal),
                                color = colors.textSecondary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Button(
                        onClick = onStartRideWithProfile,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.Black),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("RESUME WORKOUT", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        // Section Title
        Text(
            text = "WHO IS RIDING TODAY?",
            color = colors.textPrimary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Profile Cards Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 270.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(profiles) { profile ->
                val isCurrentActive = profile.id == activeProfileId
                RiderLoginCard(
                    profile = profile,
                    isCurrentActive = isCurrentActive,
                    onStartRide = {
                        viewModel.selectProfileAndStartRide(profile) {
                            onStartRideWithProfile()
                        }
                    },
                    onDelete = { viewModel.deleteProfile(profile) },
                    onEdit = { editingProfile = profile },
                    canDelete = profiles.size > 1
                )
            }
        }

        // Bottom status note
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Selecting your profile loads your custom skin & units, connects bike telemetry, and begins your ride automatically.",
            color = colors.textSecondary.copy(alpha = 0.7f),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun RiderLoginCard(
    profile: RiderProfile,
    isCurrentActive: Boolean,
    onStartRide: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    canDelete: Boolean
) {
    val colors = PeloDeckTheme.colors
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isCurrentActive) 2.dp else 1.dp,
                color = if (isCurrentActive) colors.primary else colors.panelBorder,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header row with active indicator & delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isCurrentActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.primary.copy(alpha = 0.15f))
                            .border(1.dp, colors.primary, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PREVIOUS RIDER",
                            color = colors.primary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(1.dp))
                }

                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit profile", tint = colors.primary, modifier = Modifier.size(18.dp))
                }
                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = colors.textSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceVariant)
                    .border(2.dp, if (isCurrentActive) colors.primary else colors.panelBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (profile.profileImageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(profile.profileImageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = profile.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Rider Name
            Text(
                text = profile.name,
                color = colors.textPrimary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Stats / Preferences Pill
            Text(
                text = "${profile.weightKg.toInt()} kg · ${profile.preferredUnits} · ${profile.preferredSkin.uppercase()}",
                color = colors.accent,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Big Tactile START RIDE Action Button
            Button(
                onClick = onStartRide,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "START RIDE",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun CreateRiderProfileDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, weightKg: Double, imgUri: String?, skin: String, units: String) -> Unit
) {
    val colors = PeloDeckTheme.colors
    var name by remember { mutableStateOf("") }
    var weightInput by remember { mutableStateOf("75") }
    var selectedSkin by remember { mutableStateOf(ThemeSkin.WINAMP.key) }
    var selectedUnits by remember { mutableStateOf("IMPERIAL") }
    var selectedImageUri by remember { mutableStateOf<String?>("asset:///avatars/avatar_1.png") }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "CREATE RIDER PROFILE",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = colors.primary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Rider Name", color = colors.textSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text("Weight (kg)", color = colors.textSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Profile Avatar", color = colors.textSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf(
                        "avatar_cinnamoroll" to "CINNAMOROLL",
                        "avatar_1" to "AVATAR 1",
                        "avatar_2" to "AVATAR 2",
                        "avatar_3" to "AVATAR 3"
                    ).forEach { (av, label) ->
                        val uri = "asset:///avatars/$av.png"
                        FilterChip(
                            selected = selectedImageUri == uri,
                            onClick = { selectedImageUri = uri },
                            label = { Text(label, fontSize = 10.sp) }
                        )
                    }
                    Button(
                        onClick = { filePicker.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.buttonBackground)
                    ) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Picker", fontSize = 10.sp)
                    }
                }

                Text("Preferred Skin", color = colors.textSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ThemeSkin.entries.forEach { skin ->
                        FilterChip(
                            selected = selectedSkin == skin.key,
                            onClick = { selectedSkin = skin.key },
                            label = { Text(skin.displayName, fontSize = 10.sp) }
                        )
                    }
                }

                Text("Units", color = colors.textSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedUnits == "IMPERIAL",
                        onClick = { selectedUnits = "IMPERIAL" },
                        label = { Text("Imperial (Miles/lbs)", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedUnits == "METRIC",
                        onClick = { selectedUnits = "METRIC" },
                        label = { Text("Metric (Km/kg)", fontSize = 11.sp) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val weight = weightInput.toDoubleOrNull() ?: 75.0
                    if (name.isNotBlank()) {
                        onConfirm(name, weight, selectedImageUri, selectedSkin, selectedUnits)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.Black)
            ) {
                Text("SAVE PROFILE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = colors.textSecondary)
            }
        },
        containerColor = colors.surface
    )
}
