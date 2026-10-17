package com.example.ui.screens

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.BrandGreenDark
import com.example.ui.theme.BrandGreenSurface
import com.example.ui.theme.BrandNavy
import com.example.ui.theme.DangerRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    currentProfile: UserProfile,
    onSaveProfile: (UserProfile) -> Unit,
    onClearProfile: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(currentProfile.name) }
    var builtNo by remember { mutableStateOf(currentProfile.builtNo) }
    var address by remember { mutableStateOf(currentProfile.address) }
    var email by remember { mutableStateOf(currentProfile.email) }

    var isSavedConfirmation by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Consumer Account (Optional)",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("account_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info banner: Not compulsory
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BrandGreenSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = BrandGreenDark,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Optional Profile — Not Compulsory",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = BrandGreenDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Creating an account or entering details is completely optional. If entered, your Name, Built/Consumer No, and Address are saved only on this device and automatically appear on your PDF statements.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    lineHeight = 18.sp,
                                    color = BrandNavy
                                )
                            )
                        }
                    }
                }

                // Profile form Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(BrandNavy.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = BrandNavy,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Consumer Information",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandNavy
                                )
                                Text(
                                    text = "All fields below are optional",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Consumer Name
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it; isSavedConfirmation = false },
                            label = { Text("Consumer Name (Optional)") },
                            placeholder = { Text("e.g. K. Murugan") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BrandNavy) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandNavy,
                                focusedLabelColor = BrandNavy
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("account_name_input")
                        )

                        // Built No / Consumer Number
                        OutlinedTextField(
                            value = builtNo,
                            onValueChange = { builtNo = it; isSavedConfirmation = false },
                            label = { Text("Built No / Consumer No (Optional)") },
                            placeholder = { Text("e.g. EB-04-128-445 or Flat 3B") },
                            leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null, tint = BrandNavy) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandNavy,
                                focusedLabelColor = BrandNavy
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("account_built_no_input")
                        )

                        // Address
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it; isSavedConfirmation = false },
                            label = { Text("Address / Premises (Optional)") },
                            placeholder = { Text("e.g. 14, Anna Nagar, Chennai") },
                            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = BrandNavy) },
                            singleLine = false,
                            maxLines = 3,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandNavy,
                                focusedLabelColor = BrandNavy
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("account_address_input")
                        )

                        // Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; isSavedConfirmation = false },
                            label = { Text("Email Address (Optional)") },
                            placeholder = { Text("e.g. consumer@example.com") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = BrandNavy) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandNavy,
                                focusedLabelColor = BrandNavy
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("account_email_input")
                        )
                    }
                }

                // Save and Clear Buttons
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            val newProfile = UserProfile(
                                name = name.trim(),
                                builtNo = builtNo.trim(),
                                address = address.trim(),
                                email = email.trim()
                            )
                            onSaveProfile(newProfile)
                            isSavedConfirmation = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSavedConfirmation) BrandGreenDark else BrandNavy,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_account_btn")
                    ) {
                        Icon(
                            imageVector = if (isSavedConfirmation) Icons.Default.Check else Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSavedConfirmation) "Details Saved on Device!" else "Save Consumer Details",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (currentProfile.isConfigured || name.isNotBlank() || builtNo.isNotBlank()) {
                        OutlinedButton(
                            onClick = {
                                name = ""
                                builtNo = ""
                                address = ""
                                email = ""
                                onClearProfile()
                                isSavedConfirmation = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = DangerRed
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("clear_account_btn")
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = DangerRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear Details", color = DangerRed)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
