package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.BillResultScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MeterPhotoScreen
import com.example.ui.screens.ReadingEntryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.PdfExporter

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            MyApplicationTheme(darkTheme = isDarkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val historyRecords by viewModel.historyRecords.collectAsState()
    val latestRecord by viewModel.latestSavedRecord.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val rateFixingConfig by viewModel.rateFixingConfig.collectAsState()

    val stateName by viewModel.stateName.collectAsState()
    val connectionType by viewModel.connectionType.collectAsState()
    val billingPeriod by viewModel.billingPeriod.collectAsState()
    val previousReading by viewModel.previousReadingInput.collectAsState()
    val currentReading by viewModel.currentReadingInput.collectAsState()
    val previousDate by viewModel.previousReadingDate.collectAsState()
    val currentDate by viewModel.currentReadingDate.collectAsState()
    val meterPhotoUri by viewModel.meterPhotoUri.collectAsState()
    val calculationResult by viewModel.calculationResult.collectAsState()
    val isResultSaved by viewModel.isResultSaved.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    // Handle back button on Android
    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        viewModel.navigateBack()
    }

    when (currentScreen) {
        AppScreen.HOME -> {
            HomeScreen(
                latestRecord = latestRecord,
                userProfile = userProfile,
                onNavigateToReadings = { viewModel.navigateTo(AppScreen.READING_ENTRY) },
                onNavigateToPhoto = { viewModel.navigateTo(AppScreen.METER_PHOTO) },
                onNavigateToHistory = { viewModel.navigateTo(AppScreen.HISTORY) },
                onNavigateToSettings = { viewModel.navigateTo(AppScreen.SETTINGS) },
                onNavigateToAccount = { viewModel.navigateTo(AppScreen.ACCOUNT) }
            )
        }

        AppScreen.METER_PHOTO -> {
            MeterPhotoScreen(
                currentPhotoUri = meterPhotoUri,
                onPhotoSelected = { viewModel.setMeterPhoto(it) },
                onNavigateBack = { viewModel.navigateBack() },
                onContinueToReadings = { viewModel.navigateTo(AppScreen.READING_ENTRY) }
            )
        }

        AppScreen.READING_ENTRY -> {
            ReadingEntryScreen(
                stateName = stateName,
                connectionType = connectionType,
                billingPeriod = billingPeriod,
                previousReadingInput = previousReading,
                currentReadingInput = currentReading,
                previousReadingDate = previousDate,
                currentReadingDate = currentDate,
                meterPhotoUri = meterPhotoUri,
                rateFixingConfig = rateFixingConfig,
                onRateFixingChange = { viewModel.updateRateFixingConfig(it) },
                onConnectionTypeChange = { viewModel.updateConnectionType(it) },
                onBillingPeriodChange = { viewModel.updateBillingPeriod(it) },
                onPreviousReadingChange = { viewModel.updatePreviousReading(it) },
                onCurrentReadingChange = { viewModel.updateCurrentReading(it) },
                onPreviousDateChange = { viewModel.updatePreviousDate(it) },
                onCurrentDateChange = { viewModel.updateCurrentDate(it) },
                onNavigateBack = { viewModel.navigateBack() },
                onNavigateToPhoto = { viewModel.navigateTo(AppScreen.METER_PHOTO) },
                onCalculateBill = { viewModel.calculateBill() }
            )
        }

        AppScreen.BILL_RESULT -> {
            calculationResult?.let { result ->
                BillResultScreen(
                    result = result,
                    isSaved = isResultSaved,
                    onSaveResult = { viewModel.saveCalculation() },
                    onExportPdf = { PdfExporter.generateAndShareBillPdf(context, result) },
                    onCalculateAgain = {
                        viewModel.resetCalculationForm()
                        viewModel.navigateTo(AppScreen.READING_ENTRY)
                    },
                    onReturnHome = { viewModel.navigateTo(AppScreen.HOME) },
                    onNavigateBack = { viewModel.navigateBack() }
                )
            } ?: run {
                viewModel.navigateTo(AppScreen.READING_ENTRY)
            }
        }

        AppScreen.HISTORY -> {
            HistoryScreen(
                records = historyRecords,
                onDeleteRecord = { viewModel.deleteRecord(it) },
                onExportPdf = { PdfExporter.generateAndShareRecordPdf(context, it) },
                onClearAll = { viewModel.clearAllHistory() },
                onNavigateBack = { viewModel.navigateBack() }
            )
        }

        AppScreen.SETTINGS -> {
            SettingsScreen(
                defaultState = stateName,
                defaultConnectionType = connectionType,
                isDarkMode = isDarkMode,
                tariffInfo = viewModel.tariffInfo,
                onConnectionTypeChange = { viewModel.updateConnectionType(it) },
                onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                onNavigateBack = { viewModel.navigateBack() }
            )
        }

        AppScreen.ACCOUNT -> {
            AccountScreen(
                currentProfile = userProfile,
                onSaveProfile = { viewModel.saveUserProfile(it) },
                onClearProfile = { viewModel.clearUserProfile() },
                onNavigateBack = { viewModel.navigateBack() }
            )
        }
    }
}
