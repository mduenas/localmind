package com.markduenas.localmind.billing

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class PricingScheduleTest {

    @Test
    fun usesLaunchIntroPricingBeforeScheduleStarts() {
        val date = LocalDate(2026, 3, 1)

        assertEquals("\$24.99", PricingSchedule.fallbackPrice(ProductIds.PREMIUM_LIFETIME, date))
        assertEquals("\$3.99", PricingSchedule.fallbackPrice(ProductIds.PREMIUM_MONTHLY, date))
    }

    @Test
    fun usesLaunchIntroPricingInMarch() {
        val date = LocalDate(2026, 3, 19)

        assertEquals("\$24.99", PricingSchedule.fallbackPrice(ProductIds.PREMIUM_LIFETIME, date))
        assertEquals("\$3.99", PricingSchedule.fallbackPrice(ProductIds.PREMIUM_MONTHLY, date))
        assertEquals("Launch Intro pricing through 2026-05-31", PricingSchedule.phaseNotice(date))
    }

    @Test
    fun usesStandardPricingInJune() {
        val date = LocalDate(2026, 6, 15)

        assertEquals("\$29.99", PricingSchedule.fallbackPrice(ProductIds.PREMIUM_LIFETIME, date))
        assertEquals("\$4.99", PricingSchedule.fallbackPrice(ProductIds.PREMIUM_MONTHLY, date))
    }

    @Test
    fun usesMaturePricingInOctober() {
        val date = LocalDate(2026, 10, 15)

        assertEquals("\$34.99", PricingSchedule.fallbackPrice(ProductIds.PREMIUM_LIFETIME, date))
        assertEquals("\$5.99", PricingSchedule.fallbackPrice(ProductIds.PREMIUM_MONTHLY, date))
        assertEquals("Mature pricing", PricingSchedule.phaseNotice(date))
    }
}
