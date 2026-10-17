package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BillCalculationResult
import com.example.ui.components.DisclaimerCard
import com.example.ui.components.TariffStatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillResultScreen(
    result: BillCalculationResult,
    isSaved: Boolean,
    onSaveResult: () -> Unit,
    onExportPdf: () -> Unit,
    onCalculateAgain: () -> Unit,
    onReturnHome: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDetailedBreakdown by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Calculation Result",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("result_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onExportPdf,
                        modifier = Modifier.testTag("result_export_pdf_icon_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Export PDF",
                            tint = BrandGreenDark
                        )
                    }
                    IconButton(
                        onClick = onReturnHome,
                        modifier = Modifier.testTag("result_home_action_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = BrandNavy
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
                // Header badge
                TariffStatusBadge(
                    state = result.state,
                    isVerified = result.tariffInfo.isVerified,
                    effectiveDate = result.tariffInfo.effectiveDate,
                    modifier = Modifier.fillMaxWidth()
                )

                // Hero Result Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bill_estimate_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BrandNavy
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "YOUR ELECTRICITY BILL ESTIMATE",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = BrandGreenLight,
                            letterSpacing = 1.2.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "₹${"%.2f".format(result.estimatedTotal)}",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Text(
                            text = "Estimated for ${result.billingPeriod.displayName.substringBefore(" (")}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Summary grid inside hero card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (result.userProfile?.isConfigured == true) {
                                    if (result.userProfile.name.isNotBlank()) {
                                        ResultSummaryRow("Consumer", result.userProfile.name)
                                    }
                                    if (result.userProfile.builtNo.isNotBlank()) {
                                        ResultSummaryRow("Built No / Consumer No", result.userProfile.builtNo)
                                    }
                                }
                                ResultSummaryRow("Connection Type", result.connectionType.displayName)
                                if (result.isCustomRate) {
                                    ResultSummaryRow("Rate Mode", "Custom Rate Fixing (₹${result.customRatePerKwh}/kWh)")
                                } else {
                                    ResultSummaryRow("State", result.state)
                                }
                                ResultSummaryRow("Previous Reading", "${result.previousReading.toInt()} kWh")
                                ResultSummaryRow("Current Reading", "${result.currentReading.toInt()} kWh")
                                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                                ResultSummaryRow(
                                    "Total Consumption",
                                    "${result.unitsConsumed.toInt()} units (kWh)",
                                    isHighlight = true
                                )
                            }
                        }
                    }
                }

                // Itemized Breakdown Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TableChart,
                                    contentDescription = null,
                                    tint = BrandNavy,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Itemized Bill Breakdown",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandNavy
                                )
                            }

                            IconButton(
                                onClick = { showDetailedBreakdown = !showDetailedBreakdown },
                                modifier = Modifier.testTag("toggle_breakdown_btn")
                            ) {
                                Icon(
                                    imageVector = if (showDetailedBreakdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Toggle Breakdown",
                                    tint = BrandNavy
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )

                        // Standard Itemized Charges Table
                        BreakdownTableRow("Energy Charges", "₹${"%.2f".format(result.energyCharges)}")
                        BreakdownTableRow("Fixed Charges", "₹${"%.2f".format(result.fixedCharges)}")

                        if (result.adjustments > 0.0) {
                            BreakdownTableRow("Applicable Adjustments (Duty)", "₹${"%.2f".format(result.adjustments)}")
                        }

                        if (result.subsidyAmount > 0.0) {
                            BreakdownTableRow(
                                label = "Eligible Subsidy (100 Free Units)",
                                amountStr = "−₹${"%.2f".format(result.subsidyAmount)}",
                                isSubsidy = true
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )

                        BreakdownTableRow(
                            label = "Estimated Total",
                            amountStr = "₹${"%.2f".format(result.estimatedTotal)}",
                            isBold = true
                        )

                        // Telescopic Slab Details Section
                        AnimatedVisibility(visible = showDetailedBreakdown && result.slabBreakdown.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = "Slab-wise Energy Charge Calculation:",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandNavy
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                result.slabBreakdown.forEach { slab ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = slab.rangeLabel,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = if (slab.isFreeSubsidy) BrandGreenDark else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${slab.unitsInSlab.toInt()} units @ ₹${slab.ratePerUnit}/unit",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Text(
                                            text = if (slab.isFreeSubsidy) "FREE" else "₹${"%.2f".format(slab.amount)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (slab.isFreeSubsidy) BrandGreenDark else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Disclaimer
                DisclaimerCard()

                // Actions: Export PDF, Save Result, Calculate Again, Return Home
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onExportPdf,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandGreenDark,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("export_pdf_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Download / Share Bill PDF",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onSaveResult,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSaved) BrandGreenDark else BrandNavy,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_result_btn")
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.CheckCircle else Icons.Default.Bookmark,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSaved) "Calculation Saved to History" else "Save Result",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCalculateAgain,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("calculate_again_btn")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Calculate Again", fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = onReturnHome,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("return_home_btn")
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Return Home", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ResultSummaryRow(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = if (isHighlight) BrandGreenLight else Color.White.copy(alpha = 0.85f),
            fontSize = if (isHighlight) 14.sp else 13.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = if (isHighlight) 15.sp else 13.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun BreakdownTableRow(
    label: String,
    amountStr: String,
    isSubsidy: Boolean = false,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isSubsidy) BrandGreenDark else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = amountStr,
            style = if (isBold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isSubsidy) BrandGreenDark else if (isBold) BrandNavy else MaterialTheme.colorScheme.onSurface
        )
    }
}
