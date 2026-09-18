package com.example.ui.screens.profile

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.components.AvatarSelectorDialog
import com.example.ui.components.LensDropdownField
import com.example.ui.components.LensRadioGroup
import com.example.ui.components.LensTextField
import com.example.ui.components.POPULAR_CITIES
import com.example.ui.viewmodel.AppViewModel
import com.example.util.AvatarData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: AppViewModel,
    onLogout: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var editFullName by remember(user) { mutableStateOf(user?.fullName ?: "") }
    var editGender by remember(user) { mutableStateOf(user?.gender ?: "Male") }
    var editMobile by remember(user) { mutableStateOf(user?.mobileNumber ?: "") }
    var editAddress by remember(user) { mutableStateOf(user?.address ?: "") }
    var editCity by remember(user) { mutableStateOf(user?.city ?: "San Francisco") }
    var editAvatarId by remember(user) { mutableIntStateOf(user?.avatarId ?: 0) }

    var showAvatarDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    if (showAvatarDialog) {
        AvatarSelectorDialog(
            selectedAvatarId = editAvatarId,
            onAvatarSelected = { editAvatarId = it },
            onDismiss = { showAvatarDialog = false }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out of LensGallery?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout(onLoggedOut = onLogout)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Profile", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.testTag("profile_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val activeAvatarId = if (isEditing) editAvatarId else (user?.avatarId ?: 0)
            val avatarOption = AvatarData.getAvatar(activeAvatarId)

            // Avatar Header
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .clickable(enabled = isEditing) { showAvatarDialog = true }
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(avatarOption.backgroundColor, CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = avatarOption.icon,
                        contentDescription = avatarOption.name,
                        tint = avatarOption.iconTint,
                        modifier = Modifier.size(50.dp)
                    )
                }
                if (isEditing) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit Avatar",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = user?.fullName ?: "User",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = user?.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Edit Profile / Save Toggle Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (isEditing) {
                    OutlinedButton(
                        onClick = {
                            isEditing = false
                            editFullName = user?.fullName ?: ""
                            editGender = user?.gender ?: "Male"
                            editMobile = user?.mobileNumber ?: ""
                            editAddress = user?.address ?: ""
                            editCity = user?.city ?: "San Francisco"
                            editAvatarId = user?.avatarId ?: 0
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.updateProfile(
                                fullName = editFullName,
                                gender = editGender,
                                mobileNumber = editMobile,
                                address = editAddress,
                                city = editCity,
                                avatarId = editAvatarId,
                                onSuccess = {
                                    isEditing = false
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Profile updated successfully!")
                                    }
                                },
                                onError = { error ->
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(error)
                                    }
                                }
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("profile_save_button")
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Changes")
                    }
                } else {
                    Button(
                        onClick = { isEditing = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("profile_edit_button")
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Profile")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Profile Info Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (isEditing) {
                        // Editable Fields
                        LensTextField(
                            value = editFullName,
                            onValueChange = { editFullName = it },
                            label = "Full Name",
                            leadingIcon = Icons.Filled.Person,
                            testTag = "edit_full_name_input"
                        )

                        LensRadioGroup(
                            selectedOption = editGender,
                            onOptionSelected = { editGender = it },
                            options = listOf("Male", "Female", "Other"),
                            label = "Gender"
                        )

                        LensTextField(
                            value = editMobile,
                            onValueChange = {
                                editMobile = it.filter { ch -> ch.isDigit() }.take(10)
                            },
                            label = "Mobile Number (10 digits)",
                            leadingIcon = Icons.Filled.Phone,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            testTag = "edit_mobile_input"
                        )

                        LensTextField(
                            value = editAddress,
                            onValueChange = { editAddress = it },
                            label = "Address",
                            leadingIcon = Icons.Filled.Home,
                            testTag = "edit_address_input"
                        )

                        LensDropdownField(
                            selectedValue = editCity,
                            onValueSelected = { editCity = it },
                            options = POPULAR_CITIES,
                            label = "City",
                            testTag = "edit_city_dropdown"
                        )
                    } else {
                        // View Mode Details
                        ProfileDetailRow(icon = Icons.Filled.Person, label = "Full Name", value = user?.fullName ?: "-")
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        ProfileDetailRow(icon = Icons.Filled.Email, label = "Email Address", value = user?.email ?: "-")
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        ProfileDetailRow(icon = Icons.Filled.Person, label = "Gender", value = user?.gender ?: "-")
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        ProfileDetailRow(icon = Icons.Filled.Phone, label = "Mobile Number", value = user?.mobileNumber ?: "-")
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        ProfileDetailRow(icon = Icons.Filled.Home, label = "Address", value = user?.address ?: "-")
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        ProfileDetailRow(icon = Icons.Filled.LocationCity, label = "City", value = user?.city ?: "-")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bonus Feature: Dark Mode Settings Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "App Theme",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = isDarkMode == null,
                            onClick = { viewModel.setDarkMode(null) },
                            label = { Text("System") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = isDarkMode == false,
                            onClick = { viewModel.setDarkMode(false) },
                            leadingIcon = { Icon(Icons.Filled.LightMode, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            label = { Text("Light") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = isDarkMode == true,
                            onClick = { viewModel.setDarkMode(true) },
                            leadingIcon = { Icon(Icons.Filled.DarkMode, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            label = { Text("Dark") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileDetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
