package com.example.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.BillingPeriod
import com.example.data.model.ConnectionType
import com.example.data.model.RateFixingConfig
import com.example.ui.components.TariffStatusBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingEntryScreen(
    stateName: String,
    connectionType: ConnectionType,
    billingPeriod: BillingPeriod,
    previousReadingInput: String,
    currentReadingInput: String,
    previousReadingDate: String,
    currentReadingDate: String,
    meterPhotoUri: String?,
    rateFixingConfig: RateFixingConfig,
    onRateFixingChange: (RateFixingConfig) -> Unit,
    onConnectionTypeChange: (ConnectionType) -> Unit,
    onBillingPeriodChange: (BillingPeriod) -> Unit,
    onPreviousReadingChange: (String) -> Unit,
    onCurrentReadingChange: (String) -> Unit,
    onPreviousDateChange: (String) -> Unit,
    onCurrentDateChange: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToPhoto: () -> Unit,
    onCalculateBill: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Parse readings for validation
    val prevNum = previousReadingInput.toDoubleOrNull()
    val currNum = currentReadingInput.toDoubleOrNull()

    var showUnitCalculationExplicitly by remember { mutableStateOf(false) }

    val hasNegativeConsumption by remember(prevNum, currNum) {
        derivedStateOf {
            prevNum != null && currNum != null && currNum < prevNum
        }
    }

    val unitsConsumed by remember(prevNum, currNum) {
        derivedStateOf {
            if (prevNum != null && currNum != null && currNum >= prevNum) {
                currNum - prevNum
            } else null
        }
    }

    val isInputValid by remember(prevNum, currNum, hasNegativeConsumption) {
        derivedStateOf {
            prevNum != null && currNum != null && !hasNegativeConsumption
        }
    }

    // Date picker dialog launcher
    fun showDatePicker(isPrevious: Boolean) {
        val calendar = Calendar.getInstance()
        val dpd = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth)
                val format = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
                val formatted = format.format(cal.time)
                if (isPrevious) {
                    onPreviousDateChange(formatted)
                } else {
                    onCurrentDateChange(formatted)
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dpd.show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Enter Meter Readings",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    if (meterPhotoUri != null) {
                        IconButton(onClick = onNavigateToPhoto) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "View Photo",
                                tint = BrandGreenDark
                            )
                        }
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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tariff badge
                TariffStatusBadge(
                    state = stateName,
                    isVerified = true,
                    effectiveDate = "01-Jul-2024",
                    modifier = Modifier.fillMaxWidth()
                )

                // Optional attached photo thumbnail banner
                if (meterPhotoUri != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = meterPhotoUri,
                                contentDescription = "Attached Meter Photo",
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Meter Photo Attached",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Inspect photo for numbers; enter readings manually below.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(
                                onClick = onNavigateToPhoto,
                                modifier = Modifier.testTag("view_meter_photo_btn")
                            ) {
                                Text("View")
                            }
                        }
                    }
                }

                // Section 1: Electricity Connection details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "1. Electricity Connection",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // State selection (Tamil Nadu default)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "State:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BrandGreenSurface)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = stateName,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrandGreenDark,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        // Connection Type Chips
                        Text(
                            text = "Connection Type:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ConnectionType.values().forEach { type ->
                                val isSelected = connectionType == type
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onConnectionTypeChange(type) },
                                    label = {
                                        Text(
                                            text = type.displayName.substringBefore(" /"),
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BrandNavy,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("connection_type_${type.name.lowercase()}")
                                )
                            }
                        }

                        // Billing Period Chips
                        Text(
                            text = "Billing Period:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BillingPeriod.values().forEach { period ->
                                val isSelected = billingPeriod == period
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onBillingPeriodChange(period) },
                                    label = {
                                        Text(
                                            text = if (period == BillingPeriod.BI_MONTHLY) "Bi-Monthly (2 Mo)" else "Monthly (1 Mo)",
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BrandGreenDark,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("billing_period_${period.name.lowercase()}")
                                )
                            }
                        }
                    }
                }

                // Section 2: Rate Fixing Option (Today's Rate per 1 kWh)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rate_fixing_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (rateFixingConfig.isEnabled) BrandGreenSurface else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (rateFixingConfig.isEnabled) BrandGreenDark else BrandNavy.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CurrencyRupee,
                                        contentDescription = null,
                                        tint = if (rateFixingConfig.isEnabled) Color.White else BrandNavy,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Rate Fixing (Today's 1 kWh Rate)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandNavy
                                    )
                                    Text(
                                        text = if (rateFixingConfig.isEnabled) "Custom rate fixing active" else "Official TANGEDCO slabs active",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (rateFixingConfig.isEnabled) BrandGreenDark else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = rateFixingConfig.isEnabled,
                                onCheckedChange = { isChecked ->
                                    onRateFixingChange(rateFixingConfig.copy(isEnabled = isChecked))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = BrandGreenDark
                                ),
                                modifier = Modifier.testTag("rate_fixing_switch")
                            )
                        }

                        if (rateFixingConfig.isEnabled) {
                            Text(
                                text = "Check and update today's rate for 1 kWh to calculate your electricity bill with custom accuracy:",
                                style = MaterialTheme.typography.bodySmall,
                                color = BrandNavy
                            )

                            // Quick preset chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(3.50, 4.70, 6.00, 7.50, 10.00).forEach { presetRate ->
                                    val isSelected = rateFixingConfig.customRatePerKwh == presetRate
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            onRateFixingChange(rateFixingConfig.copy(customRatePerKwh = presetRate))
                                        },
                                        label = { Text("₹$presetRate", fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = BrandGreenDark,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }

                            // Custom Rate input field
                            OutlinedTextField(
                                value = if (rateFixingConfig.customRatePerKwh == 0.0) "" else rateFixingConfig.customRatePerKwh.toString(),
                                onValueChange = { input ->
                                    val rate = input.toDoubleOrNull() ?: 0.0
                                    onRateFixingChange(rateFixingConfig.copy(customRatePerKwh = rate))
                                },
                                label = { Text("Today's Rate for 1 kWh (₹)") },
                                placeholder = { Text("e.g. 5.50") },
                                leadingIcon = {
                                    Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = BrandGreenDark)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandGreenDark,
                                    focusedLabelColor = BrandGreenDark
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_kwh_rate_input")
                            )

                            // Optional Fixed Charge field
                            OutlinedTextField(
                                value = if (rateFixingConfig.customFixedCharge == 0.0) "" else rateFixingConfig.customFixedCharge.toString(),
                                onValueChange = { input ->
                                    val fixed = input.toDoubleOrNull() ?: 0.0
                                    onRateFixingChange(rateFixingConfig.copy(customFixedCharge = fixed))
                                },
                                label = { Text("Optional Fixed Charge (₹)") },
                                placeholder = { Text("e.g. 0.00") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_fixed_charge_input")
                            )
                        } else {
                            Text(
                                text = "Using verified Tamil Nadu TANGEDCO telescopic slabs with first 100 free units. Toggle on above to fix a custom rate per 1 kWh.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Section 3: Meter Readings & Dates
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "2. Meter Readings & Dates",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Previous Meter Reading
                        OutlinedTextField(
                            value = previousReadingInput,
                            onValueChange = {
                                onPreviousReadingChange(it)
                                showUnitCalculationExplicitly = false
                            },
                            label = { Text("Previous Meter Reading (kWh)") },
                            placeholder = { Text("e.g. 1250") },
                            leadingIcon = {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = BrandNavy)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandNavy,
                                focusedLabelColor = BrandNavy
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("previous_reading_input")
                        )

                        // Previous Reading Date
                        OutlinedTextField(
                            value = previousReadingDate,
                            onValueChange = onPreviousDateChange,
                            label = { Text("Previous Reading Date") },
                            placeholder = { Text("Select date (e.g. 01-Aug-2024)") },
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker(isPrevious = true) }) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Pick Previous Date",
                                        tint = BrandNavy
                                    )
                                }
                            },
                            singleLine = true,
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDatePicker(isPrevious = true) }
                                .testTag("previous_date_input")
                        )

                        // Current Meter Reading
                        OutlinedTextField(
                            value = currentReadingInput,
                            onValueChange = {
                                onCurrentReadingChange(it)
                                showUnitCalculationExplicitly = false
                            },
                            label = { Text("Current Meter Reading (kWh)") },
                            placeholder = { Text("e.g. 1480") },
                            leadingIcon = {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = BrandGreenDark)
                            },
                            isError = hasNegativeConsumption,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    showUnitCalculationExplicitly = true
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandGreenDark,
                                focusedLabelColor = BrandGreenDark
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("current_reading_input")
                        )

                        // Current Reading Date
                        OutlinedTextField(
                            value = currentReadingDate,
                            onValueChange = onCurrentDateChange,
                            label = { Text("Current Reading Date") },
                            placeholder = { Text("Select date (e.g. 01-Oct-2024)") },
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker(isPrevious = false) }) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Pick Current Date",
                                        tint = BrandGreenDark
                                    )
                                }
                            },
                            singleLine = true,
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDatePicker(isPrevious = false) }
                                .testTag("current_date_input")
                        )

                        // Error / Negative Consumption Warning
                        if (hasNegativeConsumption) {
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFEF2F2)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, DangerRed.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Warning",
                                        tint = DangerRed,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Current reading ($currNum) is less than previous reading ($prevNum). Electricity consumption cannot be negative. Please verify readings.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = DangerRed,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }

                        // Button to Calculate Units Consumed
                        OutlinedButton(
                            onClick = {
                                focusManager.clearFocus()
                                showUnitCalculationExplicitly = true
                            },
                            enabled = previousReadingInput.isNotBlank() && currentReadingInput.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("calculate_units_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Calculate Units", fontWeight = FontWeight.SemiBold)
                        }

                        // Prominent Units Consumed Display Card
                        AnimatedVisibility(visible = unitsConsumed != null && (showUnitCalculationExplicitly || isInputValid)) {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = BrandGreenSurface
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.5.dp, BrandGreenDark.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                    .testTag("consumption_result_card")
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Electricity Consumption:",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = BrandNavy
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(BrandGreenDark)
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "${unitsConsumed?.toInt() ?: 0} units",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Formula: Current ($currNum) − Previous ($prevNum) = ${(unitsConsumed ?: 0.0).toInt()} units",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Text(
                                        text = "💡 Remember: 1 electricity unit = 1 kilowatt-hour (kWh)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BrandGreenDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Primary Action Button: Calculate My Bill
                Button(
                    onClick = onCalculateBill,
                    enabled = isInputValid,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandNavy,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("calculate_bill_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = BrandGreenLight,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Calculate My Bill",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
