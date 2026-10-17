package com.example

import com.example.data.model.BillingPeriod
import com.example.data.model.ConnectionType
import com.example.data.repository.TariffRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TariffCalculationTest {

    private val repository = TariffRepository()

    @Test
    fun testZeroConsumption_isZeroRupees() {
        val result = repository.calculateBill(
            state = "Tamil Nadu",
            connectionType = ConnectionType.DOMESTIC,
            billingPeriod = BillingPeriod.BI_MONTHLY,
            previousReading = 1000.0,
            currentReading = 1000.0
        )
        assertEquals(0.0, result.unitsConsumed, 0.001)
        assertEquals(0.0, result.estimatedTotal, 0.001)
        assertEquals(0.0, result.energyCharges, 0.001)
    }

    @Test
    fun testUnder100Units_isCompletelyFreeUnderSubsidy() {
        val result = repository.calculateBill(
            state = "Tamil Nadu",
            connectionType = ConnectionType.DOMESTIC,
            billingPeriod = BillingPeriod.BI_MONTHLY,
            previousReading = 1000.0,
            currentReading = 1085.0
        )
        assertEquals(85.0, result.unitsConsumed, 0.001)
        assertEquals(0.0, result.estimatedTotal, 0.001)
        assertEquals(1, result.slabBreakdown.size)
        assertTrue(result.slabBreakdown[0].isFreeSubsidy)
    }

    @Test
    fun testExample230UnitsConsumption_matchesOfficialFormula() {
        // Example from user prompt: Previous: 1250, Current: 1480 -> 230 units
        // 0-100: Free (0)
        // 101-200 (100 units): 100 * 2.35 = 235.00
        // 201-230 (30 units): 30 * 4.70 = 141.00
        // Total = 376.00
        val result = repository.calculateBill(
            state = "Tamil Nadu",
            connectionType = ConnectionType.DOMESTIC,
            billingPeriod = BillingPeriod.BI_MONTHLY,
            previousReading = 1250.0,
            currentReading = 1480.0
        )
        assertEquals(230.0, result.unitsConsumed, 0.001)
        assertEquals(376.0, result.estimatedTotal, 0.001)
        assertEquals(376.0, result.energyCharges, 0.001)
        assertEquals(0.0, result.fixedCharges, 0.001)
        assertEquals(3, result.slabBreakdown.size)
    }

    @Test
    fun testAbove500Units_appliesCorrectSlabs() {
        // Consumption 650 units:
        // 0-100: Free
        // 101-400 (300 units) @ 4.70 = 1410.00
        // 401-500 (100 units) @ 6.30 = 630.00
        // 501-600 (100 units) @ 8.40 = 840.00
        // 601-650 (50 units) @ 9.45 = 472.50
        // Total = 1410 + 630 + 840 + 472.50 = 3352.50
        val result = repository.calculateBill(
            state = "Tamil Nadu",
            connectionType = ConnectionType.DOMESTIC,
            billingPeriod = BillingPeriod.BI_MONTHLY,
            previousReading = 1000.0,
            currentReading = 1650.0
        )
        assertEquals(650.0, result.unitsConsumed, 0.001)
        assertEquals(3352.5, result.estimatedTotal, 0.001)
    }

    @Test
    fun testCommercialTariff_appliesFixedChargesAndDuty() {
        val result = repository.calculateBill(
            state = "Tamil Nadu",
            connectionType = ConnectionType.COMMERCIAL,
            billingPeriod = BillingPeriod.BI_MONTHLY,
            previousReading = 1000.0,
            currentReading = 1150.0 // 150 units > 100
        )
        assertEquals(150.0, result.unitsConsumed, 0.001)
        // 150 * 10.45 = 1567.50 energy charges
        assertEquals(1567.50, result.energyCharges, 0.001)
        assertEquals(220.0, result.fixedCharges, 0.001)
        // 5% duty = 78.38
        assertEquals(78.38, result.adjustments, 0.01)
        assertEquals(1865.88, result.estimatedTotal, 0.05)
    }

    @Test
    fun testUnverifiedState_returnsUnverifiedInfo() {
        val result = repository.calculateBill(
            state = "Karnataka",
            connectionType = ConnectionType.DOMESTIC,
            billingPeriod = BillingPeriod.BI_MONTHLY,
            previousReading = 100.0,
            currentReading = 200.0
        )
        assertFalse(result.tariffInfo.isVerified)
        assertEquals(0.0, result.estimatedTotal, 0.001)
    }

    @Test
    fun testRateFixing_calculatesWithCustomRatePerKwh() {
        val customConfig = com.example.data.model.RateFixingConfig(
            isEnabled = true,
            customRatePerKwh = 5.50,
            customFixedCharge = 50.0
        )
        val profile = com.example.data.model.UserProfile(
            name = "K. Murugan",
            builtNo = "EB-402",
            address = "Chennai",
            email = "murugan@example.com"
        )
        val result = repository.calculateBill(
            state = "Tamil Nadu",
            connectionType = ConnectionType.DOMESTIC,
            billingPeriod = BillingPeriod.BI_MONTHLY,
            previousReading = 1000.0,
            currentReading = 1200.0, // 200 units
            rateFixingConfig = customConfig,
            userProfile = profile
        )
        assertEquals(200.0, result.unitsConsumed, 0.001)
        // 200 units * 5.50 = 1100.00 + 50.0 fixed = 1150.00
        assertEquals(1100.0, result.energyCharges, 0.001)
        assertEquals(50.0, result.fixedCharges, 0.001)
        assertEquals(1150.0, result.estimatedTotal, 0.001)
        assertTrue(result.isCustomRate)
        assertEquals(5.50, result.customRatePerKwh!!, 0.001)
        assertEquals("K. Murugan", result.userProfile?.name)
        assertEquals("EB-402", result.userProfile?.builtNo)
    }
}
