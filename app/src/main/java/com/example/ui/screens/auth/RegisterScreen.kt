package com.example.ui.screens.auth

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.repository.UserRepository
import com.example.ui.components.AvatarSelectorDialog
import com.example.ui.components.LensDropdownField
import com.example.ui.components.LensPasswordField
import com.example.ui.components.LensRadioGroup
import com.example.ui.components.LensTextField
import com.example.ui.components.POPULAR_CITIES
import com.example.ui.viewmodel.AppViewModel
import com.example.util.AvatarData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit,
    onRegistrationSuccess: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var mobileNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("San Francisco") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedAvatarId by remember { mutableIntStateOf(0) }
    var showAvatarDialog by remember { mutableStateOf(false) }

    // Validation error states
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var genderError by remember { mutableStateOf<String?>(null) }
    var mobileError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var cityError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    fun validateAndSubmit() {
        var hasError = false

        if (fullName.isBlank()) {
            fullNameError = "Full name is required"
            hasError = true
        } else fullNameError = null

        if (email.isBlank()) {
            emailError = "Email address is required"
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            emailError = "Please enter a valid email address"
            hasError = true
        } else emailError = null

        if (gender.isBlank()) {
            genderError = "Please select gender"
            hasError = true
        } else genderError = null

        val digits = mobileNumber.filter { it.isDigit() }
        if (mobileNumber.isBlank()) {
            mobileError = "Mobile number is required"
            hasError = true
        } else if (digits.length != 10) {
            mobileError = "Mobile number must be exactly 10 digits"
            hasError = true
        } else mobileError = null

        if (address.isBlank()) {
            addressError = "Address is required"
            hasError = true
        } else addressError = null

        if (city.isBlank()) {
            cityError = "Please select a city"
            hasError = true
        } else cityError = null

        if (password.isBlank()) {
            passwordError = "Password is required"
            hasError = true
        } else if (password.length < 6) {
            passwordError = "Password must be at least 6 characters"
            hasError = true
        } else passwordError = null

        if (confirmPassword.isBlank()) {
            confirmPasswordError = "Please confirm your password"
            hasError = true
        } else if (password != confirmPassword) {
            confirmPasswordError = "Passwords do not match"
            hasError = true
        } else confirmPasswordError = null

        if (hasError) return

        viewModel.register(
            fullName = fullName,
            email = email,
            gender = gender,
            mobileNumber = mobileNumber,
            address = address,
            city = city,
            password = password,
            confirmPassword = confirmPassword,
            avatarId = selectedAvatarId,
            onSuccess = {
                onRegistrationSuccess()
            },
            onError = { err ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(err)
                }
            }
        )
    }

    if (showAvatarDialog) {
        AvatarSelectorDialog(
            selectedAvatarId = selectedAvatarId,
            onAvatarSelected = { selectedAvatarId = it },
            onDismiss = { showAvatarDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar picker row
            val currentAvatar = AvatarData.getAvatar(selectedAvatarId)
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .clickable { showAvatarDialog = true }
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(currentAvatar.backgroundColor, CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = currentAvatar.icon,
                        contentDescription = "Selected Avatar",
                        tint = currentAvatar.iconTint,
                        modifier = Modifier.size(44.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Change Avatar",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = "Tap to choose Avatar (${currentAvatar.name})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Full Name
                    LensTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            fullNameError = null
                        },
                        label = "Full Name *",
                        placeholder = "Alex Morgan",
                        leadingIcon = Icons.Filled.Person,
                        errorMessage = fullNameError,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        testTag = "register_full_name_input"
                    )

                    // Email Address
                    LensTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                        },
                        label = "Email Address *",
                        placeholder = "alex@example.com",
                        leadingIcon = Icons.Filled.Email,
                        errorMessage = emailError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        testTag = "register_email_input"
                    )

                    // Gender (Radio buttons)
                    LensRadioGroup(
                        selectedOption = gender,
                        onOptionSelected = {
                            gender = it
                            genderError = null
                        },
                        options = listOf("Male", "Female", "Other"),
                        label = "Gender *",
                        errorMessage = genderError
                    )

                    // Mobile Number (numeric, exactly 10 digits)
                    LensTextField(
                        value = mobileNumber,
                        onValueChange = {
                            val filtered = it.filter { ch -> ch.isDigit() }.take(10)
                            mobileNumber = filtered
                            mobileError = null
                        },
                        label = "Mobile Number (10 digits) *",
                        placeholder = "9876543210",
                        leadingIcon = Icons.Filled.Phone,
                        errorMessage = mobileError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        testTag = "register_mobile_input"
                    )

                    // Address
                    LensTextField(
                        value = address,
                        onValueChange = {
                            address = it
                            addressError = null
                        },
                        label = "Address *",
                        placeholder = "123 Market St, Suite 400",
                        leadingIcon = Icons.Filled.Home,
                        errorMessage = addressError,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        testTag = "register_address_input"
                    )

                    // City (Dropdown)
                    LensDropdownField(
                        selectedValue = city,
                        onValueSelected = {
                            city = it
                            cityError = null
                        },
                        options = POPULAR_CITIES,
                        label = "City *",
                        errorMessage = cityError,
                        testTag = "register_city_dropdown"
                    )

                    // Password
                    LensPasswordField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                        },
                        label = "Password (min 6 characters) *",
                        leadingIcon = Icons.Filled.Lock,
                        errorMessage = passwordError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        testTag = "register_password_input"
                    )

                    // Confirm Password
                    LensPasswordField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            confirmPasswordError = null
                        },
                        label = "Confirm Password *",
                        leadingIcon = Icons.Filled.Lock,
                        errorMessage = confirmPasswordError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            validateAndSubmit()
                        }),
                        testTag = "register_confirm_password_input"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            validateAndSubmit()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("register_submit_button")
                    ) {
                        Text(
                            text = "Register Account",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("navigate_to_login_button")
                ) {
                    Text(
                        text = "Sign In",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
