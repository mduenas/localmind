package com.markduenas.localmind.billing

import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

object PricingSchedule {
    data class Phase(
        val id: String,
        val label: String,
        val startsOn: LocalDate,
        val endsOn: LocalDate?,
        val lifetimePriceCents: Int,
        val monthlyPriceCents: Int,
    )

    private val phases = listOf(
        Phase(
            id = "launch_intro",
            label = "Launch Intro",
            startsOn = LocalDate(2026, 7, 16),
            endsOn = LocalDate(2026, 10, 1),
            lifetimePriceCents = 2499,
            monthlyPriceCents = 399,
        ),
        Phase(
            id = "standard",
            label = "Standard",
            startsOn = LocalDate(2026, 10, 2),
            endsOn = LocalDate(2027, 1, 31),
            lifetimePriceCents = 2999,
            monthlyPriceCents = 499,
        ),
        Phase(
            id = "mature",
            label = "Mature",
            startsOn = LocalDate(2027, 2, 1),
            endsOn = null,
            lifetimePriceCents = 3499,
            monthlyPriceCents = 599,
        ),
    )

    fun currentPhase(today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())): Phase {
        return phases.firstOrNull { phase ->
            val starts = today >= phase.startsOn
            val beforeEnd = phase.endsOn?.let { today <= it } ?: true
            starts && beforeEnd
        } ?: if (today < phases.first().startsOn) phases.first() else phases.last()
    }

    fun fallbackPrice(productId: String, today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())): String {
        val phase = currentPhase(today)
        val cents = when (productId) {
            ProductIds.PREMIUM_LIFETIME -> phase.lifetimePriceCents
            ProductIds.PREMIUM_MONTHLY -> phase.monthlyPriceCents
            else -> phase.lifetimePriceCents
        }
        return centsToUsd(cents)
    }

    fun phaseNotice(today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())): String {
        val phase = currentPhase(today)
        return if (phase.endsOn != null) {
            "${phase.label} pricing through ${phase.endsOn}"
        } else {
            "${phase.label} pricing"
        }
    }

    private fun centsToUsd(cents: Int): String {
        val dollars = cents / 100
        val remainder = cents % 100
        return "\$${dollars}.${remainder.toString().padStart(2, '0')}"
    }
}
