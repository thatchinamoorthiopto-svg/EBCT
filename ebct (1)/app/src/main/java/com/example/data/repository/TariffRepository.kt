package com.example.data.repository

import com.example.data.model.BillCalculationResult
import com.example.data.model.BillingPeriod
import com.example.data.model.ConnectionType
import com.example.data.model.SlabDetail
import com.example.data.model.TariffInfo
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class TariffRepository {

    val tamilNaduTariffInfo = TariffInfo(
        state = "Tamil Nadu",
        regulatoryAuthority = "Tamil Nadu Electricity Regulatory Commission (TNERC)",
        utilityProvider = "TANGEDCO (Tamil Nadu Generation & Distribution Corporation)",
        tariffOrderRef = "TNERC Tariff Schedule for LT Services (G.O. / Tariff Order)",
        effectiveDate = "01-July-2024",
        lastVerifiedDate = "October-2024 (Verified against official TNERC LT Tariff Schedule)",
        isVerified = true
    )

    fun getTariffInfo(state: String): TariffInfo {
        return if (state.equals("Tamil Nadu", ignoreCase = true)) {
            tamilNaduTariffInfo
        } else {
            TariffInfo(
                state = state,
                regulatoryAuthority = "State Regulatory Commission",
                utilityProvider = "State Electricity Board",
                tariffOrderRef = "Pending Tariff Verification",
                effectiveDate = "N/A",
                lastVerifiedDate = "Unverified",
                isVerified = false
            )
        }
    }

    /**
     * Calculates the estimated electricity bill according to official Tamil Nadu rules.
     * Slabs and rates follow the verified TNERC LT Tariff schedule for TANGEDCO.
     */
    fun calculateBill(
        state: String,
        connectionType: ConnectionType,
        billingPeriod: BillingPeriod,
        previousReading: Double,
        currentReading: Double,
        rateFixingConfig: com.example.data.model.RateFixingConfig = com.example.data.model.RateFixingConfig(),
        userProfile: com.example.data.model.UserProfile? = null
    ): BillCalculationResult {
        val tariffInfo = getTariffInfo(state)
        val unitsConsumed = max(0.0, currentReading - previousReading)

        if (rateFixingConfig.isEnabled) {
            val rate = rateFixingConfig.customRatePerKwh
            val energyCost = unitsConsumed * rate
            val fixed = rateFixingConfig.customFixedCharge
            val total = roundToTwoDecimals(energyCost + fixed)
            return BillCalculationResult(
                state = state,
                connectionType = connectionType,
                billingPeriod = billingPeriod,
                previousReading = previousReading,
                currentReading = currentReading,
                unitsConsumed = unitsConsumed,
                slabBreakdown = listOf(
                    SlabDetail(
                        rangeLabel = "Custom Fixed Rate @ ₹${rate}/kWh",
                        unitsInSlab = unitsConsumed,
                        ratePerUnit = rate,
                        amount = roundToTwoDecimals(energyCost)
                    )
                ),
                energyCharges = roundToTwoDecimals(energyCost),
                fixedCharges = fixed,
                adjustments = 0.0,
                subsidyAmount = 0.0,
                estimatedTotal = total,
                tariffInfo = tariffInfo,
                isCustomRate = true,
                customRatePerKwh = rate,
                userProfile = userProfile
            )
        }

        if (!tariffInfo.isVerified) {
            return BillCalculationResult(
                state = state,
                connectionType = connectionType,
                billingPeriod = billingPeriod,
                previousReading = previousReading,
                currentReading = currentReading,
                unitsConsumed = unitsConsumed,
                slabBreakdown = emptyList(),
                energyCharges = 0.0,
                fixedCharges = 0.0,
                adjustments = 0.0,
                subsidyAmount = 0.0,
                estimatedTotal = 0.0,
                tariffInfo = tariffInfo,
                userProfile = userProfile
            )
        }

        val baseResult = when (connectionType) {
            ConnectionType.DOMESTIC -> calculateDomesticBill(
                units = unitsConsumed,
                billingPeriod = billingPeriod,
                previousReading = previousReading,
                currentReading = currentReading,
                tariffInfo = tariffInfo
            )
            ConnectionType.COMMERCIAL -> calculateCommercialBill(
                units = unitsConsumed,
                billingPeriod = billingPeriod,
                previousReading = previousReading,
                currentReading = currentReading,
                tariffInfo = tariffInfo
            )
            ConnectionType.INDUSTRIAL -> calculateIndustrialBill(
                units = unitsConsumed,
                billingPeriod = billingPeriod,
                previousReading = previousReading,
                currentReading = currentReading,
                tariffInfo = tariffInfo
            )
        }
        return baseResult.copy(userProfile = userProfile)
    }

    /**
     * Domestic LT-IA Tariff:
     * Tamil Nadu bi-monthly domestic tariff:
     * - First 100 units free (Govt subsidy)
     * - Threshold: 500 units bi-monthly (250 units monthly)
     * Up to 500 units:
     *   0-100: Free (₹0.00)
     *   101-200: ₹2.35/unit
     *   201-400: ₹4.70/unit
     *   401-500: ₹6.30/unit
     * Above 500 units:
     *   0-100: Free (₹0.00)
     *   101-400: ₹4.70/unit
     *   401-500: ₹6.30/unit
     *   501-600: ₹8.40/unit
     *   601-800: ₹9.45/unit
     *   801-1000: ₹10.50/unit
     *   >1000: ₹11.55/unit
     * Fixed charges: ₹0.00 for domestic consumers in Tamil Nadu.
     */
    private fun calculateDomesticBill(
        units: Double,
        billingPeriod: BillingPeriod,
        previousReading: Double,
        currentReading: Double,
        tariffInfo: TariffInfo
    ): BillCalculationResult {
        val slabs = mutableListOf<SlabDetail>()
        val multiplier = billingPeriod.multiplier // 1.0 for Bi-monthly, 0.5 for Monthly

        val freeUnitsThreshold = 100.0 * multiplier
        val tier500Threshold = 500.0 * multiplier

        var energyCharges = 0.0
        var remainingUnits = units

        val freeUnitsCount = min(remainingUnits, freeUnitsThreshold)
        if (freeUnitsCount > 0) {
            slabs.add(
                SlabDetail(
                    rangeLabel = "0 - ${freeUnitsThreshold.toInt()} units (Free Subsidy)",
                    unitsInSlab = freeUnitsCount,
                    ratePerUnit = 0.0,
                    amount = 0.0,
                    isFreeSubsidy = true
                )
            )
            remainingUnits -= freeUnitsCount
        }

        if (units <= tier500Threshold) {
            // Slabs up to 500 units
            // Slab 2: 101-200 units @ 2.35
            val slab2Max = (200.0 * multiplier) - freeUnitsThreshold
            if (remainingUnits > 0) {
                val inSlab = min(remainingUnits, slab2Max)
                val cost = inSlab * 2.35
                energyCharges += cost
                slabs.add(
                    SlabDetail(
                        rangeLabel = "${(100 * multiplier).toInt() + 1} - ${(200 * multiplier).toInt()} units",
                        unitsInSlab = inSlab,
                        ratePerUnit = 2.35,
                        amount = roundToTwoDecimals(cost)
                    )
                )
                remainingUnits -= inSlab
            }

            // Slab 3: 201-400 units @ 4.70
            val slab3Max = (400.0 * multiplier) - (200.0 * multiplier)
            if (remainingUnits > 0) {
                val inSlab = min(remainingUnits, slab3Max)
                val cost = inSlab * 4.70
                energyCharges += cost
                slabs.add(
                    SlabDetail(
                        rangeLabel = "${(200 * multiplier).toInt() + 1} - ${(400 * multiplier).toInt()} units",
                        unitsInSlab = inSlab,
                        ratePerUnit = 4.70,
                        amount = roundToTwoDecimals(cost)
                    )
                )
                remainingUnits -= inSlab
            }

            // Slab 4: 401-500 units @ 6.30
            val slab4Max = (500.0 * multiplier) - (400.0 * multiplier)
            if (remainingUnits > 0) {
                val inSlab = min(remainingUnits, slab4Max)
                val cost = inSlab * 6.30
                energyCharges += cost
                slabs.add(
                    SlabDetail(
                        rangeLabel = "${(400 * multiplier).toInt() + 1} - ${(500 * multiplier).toInt()} units",
                        unitsInSlab = inSlab,
                        ratePerUnit = 6.30,
                        amount = roundToTwoDecimals(cost)
                    )
                )
                remainingUnits -= inSlab
            }
        } else {
            // Consumption above 500 units bi-monthly
            // Slab 2: 101-400 units @ 4.70
            val slab2Max = (400.0 * multiplier) - freeUnitsThreshold
            if (remainingUnits > 0) {
                val inSlab = min(remainingUnits, slab2Max)
                val cost = inSlab * 4.70
                energyCharges += cost
                slabs.add(
                    SlabDetail(
                        rangeLabel = "${(100 * multiplier).toInt() + 1} - ${(400 * multiplier).toInt()} units",
                        unitsInSlab = inSlab,
                        ratePerUnit = 4.70,
                        amount = roundToTwoDecimals(cost)
                    )
                )
                remainingUnits -= inSlab
            }

            // Slab 3: 401-500 units @ 6.30
            val slab3Max = (500.0 * multiplier) - (400.0 * multiplier)
            if (remainingUnits > 0) {
                val inSlab = min(remainingUnits, slab3Max)
                val cost = inSlab * 6.30
                energyCharges += cost
                slabs.add(
                    SlabDetail(
                        rangeLabel = "${(400 * multiplier).toInt() + 1} - ${(500 * multiplier).toInt()} units",
                        unitsInSlab = inSlab,
                        ratePerUnit = 6.30,
                        amount = roundToTwoDecimals(cost)
                    )
                )
                remainingUnits -= inSlab
            }

            // Slab 4: 501-600 units @ 8.40
            val slab4Max = (600.0 * multiplier) - (500.0 * multiplier)
            if (remainingUnits > 0) {
                val inSlab = min(remainingUnits, slab4Max)
                val cost = inSlab * 8.40
                energyCharges += cost
                slabs.add(
                    SlabDetail(
                        rangeLabel = "${(500 * multiplier).toInt() + 1} - ${(600 * multiplier).toInt()} units",
                        unitsInSlab = inSlab,
                        ratePerUnit = 8.40,
                        amount = roundToTwoDecimals(cost)
                    )
                )
                remainingUnits -= inSlab
            }

            // Slab 5: 601-800 units @ 9.45
            val slab5Max = (800.0 * multiplier) - (600.0 * multiplier)
            if (remainingUnits > 0) {
                val inSlab = min(remainingUnits, slab5Max)
                val cost = inSlab * 9.45
                energyCharges += cost
                slabs.add(
                    SlabDetail(
                        rangeLabel = "${(600 * multiplier).toInt() + 1} - ${(800 * multiplier).toInt()} units",
                        unitsInSlab = inSlab,
                        ratePerUnit = 9.45,
                        amount = roundToTwoDecimals(cost)
                    )
                )
                remainingUnits -= inSlab
            }

            // Slab 6: 801-1000 units @ 10.50
            val slab6Max = (1000.0 * multiplier) - (800.0 * multiplier)
            if (remainingUnits > 0) {
                val inSlab = min(remainingUnits, slab6Max)
                val cost = inSlab * 10.50
                energyCharges += cost
                slabs.add(
                    SlabDetail(
                        rangeLabel = "${(800 * multiplier).toInt() + 1} - ${(1000 * multiplier).toInt()} units",
                        unitsInSlab = inSlab,
                        ratePerUnit = 10.50,
                        amount = roundToTwoDecimals(cost)
                    )
                )
                remainingUnits -= inSlab
            }

            // Slab 7: >1000 units @ 11.55
            if (remainingUnits > 0) {
                val inSlab = remainingUnits
                val cost = inSlab * 11.55
                energyCharges += cost
                slabs.add(
                    SlabDetail(
                        rangeLabel = "Above ${(1000 * multiplier).toInt()} units",
                        unitsInSlab = inSlab,
                        ratePerUnit = 11.55,
                        amount = roundToTwoDecimals(cost)
                    )
                )
                remainingUnits -= inSlab
            }
        }

        // Subsidized savings provided by Govt of Tamil Nadu (worth up to ₹470 for the 100 units)
        val subsidyValue = freeUnitsCount * 4.70

        return BillCalculationResult(
            state = "Tamil Nadu",
            connectionType = ConnectionType.DOMESTIC,
            billingPeriod = billingPeriod,
            previousReading = previousReading,
            currentReading = currentReading,
            unitsConsumed = units,
            slabBreakdown = slabs,
            energyCharges = roundToTwoDecimals(energyCharges),
            fixedCharges = 0.0,
            adjustments = 0.0,
            subsidyAmount = roundToTwoDecimals(subsidyValue),
            estimatedTotal = roundToTwoDecimals(energyCharges),
            tariffInfo = tariffInfo
        )
    }

    /**
     * Commercial LT-V Tariff:
     * - Under 100 units bi-monthly: ₹6.65/unit
     * - Above 100 units bi-monthly: ₹10.45/unit flat
     * - Fixed charge: ₹220 bi-monthly (₹110/month)
     * - Electricity Duty: 5%
     */
    private fun calculateCommercialBill(
        units: Double,
        billingPeriod: BillingPeriod,
        previousReading: Double,
        currentReading: Double,
        tariffInfo: TariffInfo
    ): BillCalculationResult {
        val multiplier = billingPeriod.multiplier
        val threshold = 100.0 * multiplier
        val slabs = mutableListOf<SlabDetail>()

        val rate = if (units <= threshold) 6.65 else 10.45
        val energyCost = units * rate
        slabs.add(
            SlabDetail(
                rangeLabel = if (units <= threshold) "All units (<= ${threshold.toInt()} units)" else "All units (> ${threshold.toInt()} units)",
                unitsInSlab = units,
                ratePerUnit = rate,
                amount = roundToTwoDecimals(energyCost)
            )
        )

        val fixedCharges = roundToTwoDecimals(220.0 * multiplier)
        val electricityDuty = roundToTwoDecimals(energyCost * 0.05)
        val total = roundToTwoDecimals(energyCost + fixedCharges + electricityDuty)

        return BillCalculationResult(
            state = "Tamil Nadu",
            connectionType = ConnectionType.COMMERCIAL,
            billingPeriod = billingPeriod,
            previousReading = previousReading,
            currentReading = currentReading,
            unitsConsumed = units,
            slabBreakdown = slabs,
            energyCharges = roundToTwoDecimals(energyCost),
            fixedCharges = fixedCharges,
            adjustments = electricityDuty,
            subsidyAmount = 0.0,
            estimatedTotal = total,
            tariffInfo = tariffInfo
        )
    }

    /**
     * Industrial LT-IIIB Tariff:
     * - Flat energy charge: ₹7.50 / unit
     * - Fixed demand charge: ₹562.00 bi-monthly (₹281/month)
     * - Electricity Duty: 5%
     */
    private fun calculateIndustrialBill(
        units: Double,
        billingPeriod: BillingPeriod,
        previousReading: Double,
        currentReading: Double,
        tariffInfo: TariffInfo
    ): BillCalculationResult {
        val multiplier = billingPeriod.multiplier
        val slabs = mutableListOf<SlabDetail>()

        val energyCost = units * 7.50
        slabs.add(
            SlabDetail(
                rangeLabel = "Industrial Energy Charges",
                unitsInSlab = units,
                ratePerUnit = 7.50,
                amount = roundToTwoDecimals(energyCost)
            )
        )

        val fixedCharges = roundToTwoDecimals(562.0 * multiplier)
        val electricityDuty = roundToTwoDecimals(energyCost * 0.05)
        val total = roundToTwoDecimals(energyCost + fixedCharges + electricityDuty)

        return BillCalculationResult(
            state = "Tamil Nadu",
            connectionType = ConnectionType.INDUSTRIAL,
            billingPeriod = billingPeriod,
            previousReading = previousReading,
            currentReading = currentReading,
            unitsConsumed = units,
            slabBreakdown = slabs,
            energyCharges = roundToTwoDecimals(energyCost),
            fixedCharges = fixedCharges,
            adjustments = electricityDuty,
            subsidyAmount = 0.0,
            estimatedTotal = total,
            tariffInfo = tariffInfo
        )
    }

    private fun roundToTwoDecimals(value: Double): Double {
        return (value * 100.0).roundToInt() / 100.0
    }
}
