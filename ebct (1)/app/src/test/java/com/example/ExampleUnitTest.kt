package com.example

import com.example.data.model.BillingPeriod
import com.example.data.model.ConnectionType
import com.example.data.model.RateFixingConfig
import com.example.data.model.UserProfile
import com.example.data.repository.TariffRepository
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    private val repository = TariffRepository()

    @Test
    fun testDomesticBiMonthlyCalculation_230Units() {
        val result = repository.calculateBill(
            state = "Tamil Nadu",
            connectionType = ConnectionType.DOMESTIC,
            billingPeriod = BillingPeriod.BI_MONTHLY,
            previousReading = 1250.0,
            currentReading = 1480.0
        )

        assertEquals(230.0, result.unitsConsumed, 0.001)
        assertTrue(result.tariffInfo.isVerified)
        assertTrue(result.estimatedTotal > 0.0)
        // Ensure subsidy for free 100 units is counted
        assertTrue(result.subsidyAmount > 0.0)
    }

    @Test
    fun testDomesticZeroUnits_isFree() {
        val result = repository.calculateBill(
            state = "Tamil Nadu",
            connectionType = ConnectionType.DOMESTIC,
            billingPeriod = BillingPeriod.BI_MONTHLY,
            previousReading = 1000.0,
            currentReading = 1000.0
        )

        assertEquals(0.0, result.unitsConsumed, 0.001)
        assertEquals(0.0, result.estimatedTotal, 0.001)
    }

    @Test
    fun testCustomRateFixing() {
        val customConfig = RateFixingConfig(
            isEnabled = true,
            customRatePerKwh = 6.50,
            customFixedCharge = 20.0
        )
        val profile = UserProfile(
            name = "R. Selvam",
            builtNo = "EB-9876",
            address = "Coimbatore",
            email = "selvam@example.com"
        )

        val result = repository.calculateBill(
            state = "Tamil Nadu",
            connectionType = ConnectionType.DOMESTIC,
            billingPeriod = BillingPeriod.BI_MONTHLY,
            previousReading = 100.0,
            currentReading = 200.0,
            rateFixingConfig = customConfig,
            userProfile = profile
        )

        assertEquals(100.0, result.unitsConsumed, 0.001)
        assertTrue(result.isCustomRate)
        assertEquals(6.50, result.customRatePerKwh!!, 0.001)
        // 100 * 6.50 = 650.0 + 20.0 fixed = 670.0
        assertEquals(670.0, result.estimatedTotal, 0.001)
        assertNotNull(result.userProfile)
        assertEquals("R. Selvam", result.userProfile?.name)
    }
}
