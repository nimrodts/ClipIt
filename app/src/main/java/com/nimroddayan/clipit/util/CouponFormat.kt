package com.nimroddayan.clipit.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

enum class ExpiryUrgency { Normal, Soon, Expired }

data class ExpiryLabel(val text: String, val urgency: ExpiryUrgency, val daysLeft: Long)

/**
 * Relative expiry for a coupon card: "Expires today", "Expires tomorrow", "N days left",
 * "Until 14 Mar", or "Expired N days ago". Days are calendar days in [zone].
 */
fun expiryLabel(
        expirationDate: Long,
        now: Long = System.currentTimeMillis(),
        zone: ZoneId = ZoneId.systemDefault(),
        locale: Locale = Locale.getDefault(),
): ExpiryLabel {
    val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
    val expiry = Instant.ofEpochMilli(expirationDate).atZone(zone).toLocalDate()
    val days = ChronoUnit.DAYS.between(today, expiry)
    return when {
        days < 0 -> {
            val ago = -days
            ExpiryLabel("Expired $ago day${if (ago == 1L) "" else "s"} ago", ExpiryUrgency.Expired, days)
        }
        days == 0L -> ExpiryLabel("Expires today", ExpiryUrgency.Expired, days)
        days == 1L -> ExpiryLabel("Expires tomorrow", ExpiryUrgency.Soon, days)
        days <= 7 -> ExpiryLabel("$days days left", ExpiryUrgency.Soon, days)
        days <= 30 -> ExpiryLabel("$days days left", ExpiryUrgency.Normal, days)
        else ->
                ExpiryLabel(
                        "Until " + expiry.format(DateTimeFormatter.ofPattern("d MMM", locale)),
                        ExpiryUrgency.Normal,
                        days
                )
    }
}

/** Full date for detail views, e.g. "22 Oct 2026". */
fun formatDate(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault(), locale: Locale = Locale.getDefault()): String =
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
                .format(DateTimeFormatter.ofPattern("d MMM yyyy", locale))

private fun moneyFormat(locale: Locale, decimals: Int): NumberFormat =
        NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = decimals
            maximumFractionDigits = decimals
            isGroupingUsed = true
        }

private fun toCents(amount: Double): BigDecimal =
        BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP).let {
            if (it.signum() == 0) BigDecimal.ZERO.setScale(2) else it
        }

/** "₪1,234.50" */
fun formatMoney(amount: Double, symbol: String, locale: Locale = Locale.getDefault()): String =
        symbol + moneyFormat(locale, 2).format(toCents(amount))

/** "₪250" for whole amounts, "₪35.90" otherwise. For compact labels such as "of ₪250". */
fun formatMoneyShort(amount: Double, symbol: String, locale: Locale = Locale.getDefault()): String {
    val cents = toCents(amount)
    val whole = cents.stripTrailingZeros().scale() <= 0
    return symbol + moneyFormat(locale, if (whole) 0 else 2).format(cents)
}

/** Parses what a person types into an amount field; accepts "12.5" and "12,5". */
fun parseAmount(text: String): Double? = text.trim().replace(',', '.').toDoubleOrNull()
