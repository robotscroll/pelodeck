package com.pelodeck.ui.profiles

import android.net.Uri
import android.content.Intent
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.pelodeck.domain.model.ThemeSkin
import com.pelodeck.ui.theme.PeloDeckTheme

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val profiles by viewModel.allProfiles.collectAsState()
    val activeProfileId by viewModel.activeProfileId.collectAsState()
    val conflictState by viewModel.conflictState.collectAsState()

    val colors = PeloDeckTheme.colors
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<RiderProfile?>(null) }

    editingProfile?.let { profile ->
        CreateProfileDialog(initial = profile, onDismiss = { editingProfile = null },
            onConfirm = { name, weight, img, skin, units ->
                viewModel.updateProfile(profile, name, weight, img, skin, units)
                editingProfile = null
            })
    }

    // Conflict Dialog when trying to switch during active ride
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
                            text = "A ride session is currently underway. Profile switching will not silently transfer this ride to ${conflict.targetProfile.name}. Would you like to finish and save your ride first, or discard it?",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = colors.textPrimary
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.resolveConflictAndFinishRide(conflict.targetProfile) },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.background)
                        ) {
                            Text("FINISH & SWITCH")
                        }
                    },
                    dismissButton = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.resolveConflictAndDiscardRide(conflict.targetProfile) }
                            ) {
                                Text("DISCARD RIDE", color = colors.secondary)
                            }
                            TextButton(
                                onClick = { viewModel.dismissConflict() }
                            ) {
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
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "RIDER PROFILES",
                    color = colors.primary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
            }

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.buttonBackground,
                    contentColor = colors.primary
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("ADD RIDER", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }

        // Profile Cards Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 280.dp),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(profiles) { profile ->
                val isActive = profile.id == activeProfileId
                ProfileCard(
                    profile = profile,
                    isActive = isActive,
                    onSelect = { viewModel.selectProfile(profile) },
                    onEdit = { editingProfile = profile },
                    onDelete = { viewModel.deleteProfile(profile) }
                )
            }
        }
    }
}

@Composable
fun ProfileCard(
    profile: RiderProfile,
    isActive: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = PeloDeckTheme.colors
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surface)
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) colors.primary else colors.panelBorder,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Avatar Image
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceVariant)
                        .border(1.dp, if (isActive) colors.primary else colors.panelBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (profile.profileImageUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(profile.profileImageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.name,
                        color = colors.textPrimary,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Weight: ${profile.weightKg.toInt()} kg | Units: ${profile.preferredUnits}",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Skin: ${profile.preferredSkin.uppercase()}",
                        color = colors.accent,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isActive) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ACTIVE RIDER",
                            color = colors.primary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Button(
                        onClick = onSelect,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.buttonBackground,
                            contentColor = colors.buttonText
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("SELECT", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }
                }

                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit profile", tint = colors.primary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = colors.textSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CreateProfileDialog(
    initial: RiderProfile? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, weightKg: Double, imgUri: String?, skin: String, units: String) -> Unit
) {
    val colors = PeloDeckTheme.colors
    val context = LocalContext.current
    var name by remember(initial) { mutableStateOf(initial?.name ?: "") }
    var weightInput by remember(initial) { mutableStateOf(initial?.weightKg?.toString() ?: "75") }
    var selectedSkin by remember(initial) { mutableStateOf(initial?.preferredSkin ?: ThemeSkin.WINAMP.key) }
    var selectedUnits by remember(initial) { mutableStateOf(initial?.preferredUnits ?: "IMPERIAL") }
    var selectedImageUri by remember(initial) { mutableStateOf<String?>(initial?.profileImageUri ?: "asset:///avatars/avatar_1.png") }

    // File Picker for custom rider photos
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            selectedImageUri = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initial == null) "CREATE RIDER PROFILE" else "EDIT RIDER PROFILE",
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
                        onClick = { filePicker.launch(arrayOf("image/*")) },
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
                    if (name.isNotBlank() && weight > 0) {
                        onConfirm(name, weight, selectedImageUri, selectedSkin, selectedUnits)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.background)
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
