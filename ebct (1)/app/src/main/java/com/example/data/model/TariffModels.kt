package com.example.data.model

enum class ConnectionType(val displayName: String, val tariffCode: String) {
    DOMESTIC("Domestic / Home", "LT-IA"),
    COMMERCIAL("Commercial / Shop", "LT-V"),
    INDUSTRIAL("Industrial", "LT-IIIB")
}

enum class BillingPeriod(val displayName: String, val multiplier: Double) {
    BI_MONTHLY("Bi-Monthly (2 Months - Standard TN)", 1.0),
    MONTHLY("Monthly (1 Month)", 0.5)
}

data class SlabDetail(
    val rangeLabel: String,
    val unitsInSlab: Double,
    val ratePerUnit: Double,
    val amount: Double,
    val isFreeSubsidy: Boolean = false
)

data class ItemizedCharge(
    val description: String,
    val amount: Double,
    val isCreditOrSubsidy: Boolean = false,
    val note: String? = null
)

data class TariffInfo(
    val state: String = "Tamil Nadu",
    val regulatoryAuthority: String = "Tamil Nadu Electricity Regulatory Commission (TNERC)",
    val utilityProvider: String = "TANGEDCO",
    val tariffOrderRef: String = "TNERC Order No. 7 / Multi-Year Tariff Schedule",
    val effectiveDate: String = "01-July-2024",
    val lastVerifiedDate: String = "October-2024 (Verified against official TNERC LT Tariff Schedule)",
    val isVerified: Boolean = true
)

data class UserProfile(
    val name: String = "",
    val builtNo: String = "",
    val address: String = "",
    val email: String = ""
) {
    val isConfigured: Boolean get() = name.isNotBlank() || builtNo.isNotBlank() || address.isNotBlank() || email.isNotBlank()
}

data class RateFixingConfig(
    val isEnabled: Boolean = false,
    val customRatePerKwh: Double = 4.70,
    val customFixedCharge: Double = 0.0,
    val lastUpdatedDate: String = "Today"
)

data class BillCalculationResult(
    val state: String,
    val connectionType: ConnectionType,
    val billingPeriod: BillingPeriod,
    val previousReading: Double,
    val currentReading: Double,
    val unitsConsumed: Double,
    val slabBreakdown: List<SlabDetail>,
    val energyCharges: Double,
    val fixedCharges: Double,
    val adjustments: Double,
    val subsidyAmount: Double,
    val estimatedTotal: Double,
    val tariffInfo: TariffInfo,
    val isCustomRate: Boolean = false,
    val customRatePerKwh: Double? = null,
    val userProfile: UserProfile? = null,
    val calculationTimestamp: Long = System.currentTimeMillis()
)
