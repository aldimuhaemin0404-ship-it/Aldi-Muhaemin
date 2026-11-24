package com.example

import com.example.domain.SavingsGoalProgress
import com.example.util.formatCompactRupiah
import com.example.util.formatRupiah
import com.example.util.startOfDayMillis
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun formatRupiah_formatsIndonesianCurrency() {
        assertEquals("Rp 150.000", formatRupiah(150_000L))
        assertEquals("Rp 0", formatRupiah(0L))
    }

    @Test
    fun formatCompactRupiah_formatsThousandsAndMillions() {
        assertEquals("250 rb", formatCompactRupiah(250_000L))
        assertEquals("1,5 jt", formatCompactRupiah(1_500_000L))
        assertEquals("2 jt", formatCompactRupiah(2_000_000L))
    }

    @Test
    fun startOfDayMillis_normalizesTimestampsOnSameDay() {
        val t1 = 1732435200000L
        val t2 = t1 + 3_600_000L
        assertEquals(startOfDayMillis(t1), startOfDayMillis(t2))
        assertTrue(startOfDayMillis(t1) <= t1)
    }

    @Test
    fun savingsGoalProgress_calculatesPercentageRemainingAndEstimatedDays() {
        val progress = SavingsGoalProgress.calculate(
            goalName = "Dana Darurat",
            targetAmount = 5_000_000L,
            totalIncome = 2_000_000L,
            effectiveDailyAverage = 250_000L
        )
        assertEquals(40, progress.percentageInt)
        assertEquals(0.4f, progress.progressFraction, 0.001f)
        assertEquals(3_000_000L, progress.remainingAmount)
        assertEquals(12, progress.estimatedDaysRemaining)
        assertFalse(progress.isAchieved)
    }

    @Test
    fun savingsGoalProgress_handlesAchievedAndZeroAverageEdgeCases() {
        val achieved = SavingsGoalProgress.calculate(
            goalName = "Modal Usaha",
            targetAmount = 1_000_000L,
            totalIncome = 1_250_000L,
            effectiveDailyAverage = 250_000L
        )
        assertTrue(achieved.isAchieved)
        assertEquals(125, achieved.percentageInt)
        assertEquals(1.0f, achieved.progressFraction, 0.001f)
        assertEquals(0L, achieved.remainingAmount)
        assertEquals(0, achieved.estimatedDaysRemaining)

        val noIncome = SavingsGoalProgress.calculate(
            goalName = "Target Baru",
            targetAmount = 3_000_000L,
            totalIncome = 0L,
            effectiveDailyAverage = 0L
        )
        assertEquals(0, noIncome.percentageInt)
        assertEquals(3_000_000L, noIncome.remainingAmount)
        assertNull(noIncome.estimatedDaysRemaining)
    }

    @Test
    fun colorCatalog_formatsHexStringsCleanly() {
        assertEquals("#0D6E4F", com.example.util.KasColorCatalog.toHexString(0xFF0D6E4FL))
        assertEquals("#E11D48", com.example.util.KasColorCatalog.toHexString(0xFFE11D48L))
    }
}
