package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BillDatabase
import com.example.data.model.BillCalculationResult
import com.example.data.model.BillRecord
import com.example.data.model.BillingPeriod
import com.example.data.model.ConnectionType
import com.example.data.model.RateFixingConfig
import com.example.data.model.UserProfile
import com.example.data.repository.BillRepository
import com.example.data.repository.TariffRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AppScreen {
    HOME,
    METER_PHOTO,
    READING_ENTRY,
    BILL_RESULT,
    HISTORY,
    SETTINGS,
    ACCOUNT
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("ebct_prefs", Context.MODE_PRIVATE)

    private val tariffRepository = TariffRepository()
    private val database = BillDatabase.getDatabase(application)
    private val billRepository = BillRepository(database.billDao())

    // Screen navigation state
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val navigationBackStack = mutableListOf<AppScreen>()

    // Optional Consumer Profile state
    private val _userProfile = MutableStateFlow(loadUserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // Rate Fixing state
    private val _rateFixingConfig = MutableStateFlow(loadRateFixingConfig())
    val rateFixingConfig: StateFlow<RateFixingConfig> = _rateFixingConfig.asStateFlow()

    // History data
    val historyRecords: StateFlow<List<BillRecord>> = billRepository.getAllRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestSavedRecord: StateFlow<BillRecord?> = billRepository.getLatestRecord()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Form state
    val stateName = MutableStateFlow("Tamil Nadu")
    val connectionType = MutableStateFlow(ConnectionType.DOMESTIC)
    val billingPeriod = MutableStateFlow(BillingPeriod.BI_MONTHLY)

    val previousReadingInput = MutableStateFlow("")
    val currentReadingInput = MutableStateFlow("")

    val previousReadingDate = MutableStateFlow(getDefaultPreviousDate())
    val currentReadingDate = MutableStateFlow(getDefaultCurrentDate())

    val meterPhotoUri = MutableStateFlow<String?>(null)

    // Calculation result state
    private val _calculationResult = MutableStateFlow<BillCalculationResult?>(null)
    val calculationResult: StateFlow<BillCalculationResult?> = _calculationResult.asStateFlow()

    private val _isResultSaved = MutableStateFlow(false)
    val isResultSaved: StateFlow<Boolean> = _isResultSaved.asStateFlow()

    // Settings
    val isDarkMode = MutableStateFlow(false)

    val tariffInfo = tariffRepository.tamilNaduTariffInfo

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            navigationBackStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (navigationBackStack.isNotEmpty()) {
            val prev = navigationBackStack.removeAt(navigationBackStack.lastIndex)
            _currentScreen.value = prev
            return true
        } else if (_currentScreen.value != AppScreen.HOME) {
            _currentScreen.value = AppScreen.HOME
            return true
        }
        return false
    }

    fun setMeterPhoto(uri: String?) {
        meterPhotoUri.value = uri
    }

    fun updateConnectionType(type: ConnectionType) {
        connectionType.value = type
    }

    fun updateBillingPeriod(period: BillingPeriod) {
        billingPeriod.value = period
    }

    fun updatePreviousReading(value: String) {
        previousReadingInput.value = value
        _isResultSaved.value = false
    }

    fun updateCurrentReading(value: String) {
        currentReadingInput.value = value
        _isResultSaved.value = false
    }

    fun updatePreviousDate(value: String) {
        previousReadingDate.value = value
    }

    fun updateCurrentDate(value: String) {
        currentReadingDate.value = value
    }

    fun updateRateFixingConfig(config: RateFixingConfig) {
        _rateFixingConfig.value = config
        prefs.edit()
            .putBoolean("rf_enabled", config.isEnabled)
            .putFloat("rf_rate", config.customRatePerKwh.toFloat())
            .putFloat("rf_fixed", config.customFixedCharge.toFloat())
            .apply()
    }

    fun saveUserProfile(profile: UserProfile) {
        _userProfile.value = profile
        prefs.edit()
            .putString("user_name", profile.name)
            .putString("user_built_no", profile.builtNo)
            .putString("user_address", profile.address)
            .putString("user_email", profile.email)
            .apply()
    }

    fun clearUserProfile() {
        _userProfile.value = UserProfile()
        prefs.edit()
            .remove("user_name")
            .remove("user_built_no")
            .remove("user_address")
            .remove("user_email")
            .apply()
    }

    fun calculateBill() {
        val prev = previousReadingInput.value.toDoubleOrNull() ?: 0.0
        val curr = currentReadingInput.value.toDoubleOrNull() ?: 0.0

        val result = tariffRepository.calculateBill(
            state = stateName.value,
            connectionType = connectionType.value,
            billingPeriod = billingPeriod.value,
            previousReading = prev,
            currentReading = curr,
            rateFixingConfig = _rateFixingConfig.value,
            userProfile = _userProfile.value
        )

        _calculationResult.value = result
        _isResultSaved.value = false
        navigateTo(AppScreen.BILL_RESULT)
    }

    fun saveCalculation() {
        val result = _calculationResult.value ?: return
        if (_isResultSaved.value) return

        viewModelScope.launch {
            val record = BillRecord(
                state = result.state,
                connectionType = result.connectionType.displayName,
                billingPeriod = result.billingPeriod.displayName,
                previousReading = result.previousReading,
                currentReading = result.currentReading,
                previousReadingDate = previousReadingDate.value,
                currentReadingDate = currentReadingDate.value,
                unitsConsumed = result.unitsConsumed,
                energyCharges = result.energyCharges,
                fixedCharges = result.fixedCharges,
                adjustments = result.adjustments,
                subsidyAmount = result.subsidyAmount,
                estimatedTotal = result.estimatedTotal,
                meterPhotoUri = meterPhotoUri.value
            )
            billRepository.saveRecord(record)
            _isResultSaved.value = true
        }
    }

    fun deleteRecord(record: BillRecord) {
        viewModelScope.launch {
            billRepository.deleteRecord(record)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            billRepository.clearAllHistory()
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        isDarkMode.value = enabled
    }

    fun resetCalculationForm() {
        previousReadingInput.value = ""
        currentReadingInput.value = ""
        meterPhotoUri.value = null
        _calculationResult.value = null
        _isResultSaved.value = false
    }

    private fun loadUserProfile(): UserProfile {
        return UserProfile(
            name = prefs.getString("user_name", "") ?: "",
            builtNo = prefs.getString("user_built_no", "") ?: "",
            address = prefs.getString("user_address", "") ?: "",
            email = prefs.getString("user_email", "") ?: ""
        )
    }

    private fun loadRateFixingConfig(): RateFixingConfig {
        return RateFixingConfig(
            isEnabled = prefs.getBoolean("rf_enabled", false),
            customRatePerKwh = prefs.getFloat("rf_rate", 4.70f).toDouble(),
            customFixedCharge = prefs.getFloat("rf_fixed", 0.0f).toDouble()
        )
    }

    private fun getDefaultCurrentDate(): String {
        val format = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        return format.format(Date())
    }

    private fun getDefaultPreviousDate(): String {
        val format = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -2) // Bi-monthly cycle 2 months earlier
        return format.format(cal.time)
    }
}
