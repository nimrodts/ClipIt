package com.nimroddayan.clipit.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class CouponFormatTest {

    private val zone = ZoneId.of("Asia/Jerusalem")
    // 11 Oct 2026, 22:30 local: late in the day, so "days left" must count calendar days, not 24h blocks.
    private val now = LocalDateTime.of(2026, 10, 11, 22, 30).atZone(zone).toInstant().toEpochMilli()

    private fun expiresOn(date: LocalDate, hour: Int = 0) =
            date.atTime(hour, 0).atZone(zone).toInstant().toEpochMilli()

    private fun label(date: LocalDate, hour: Int = 0) =
            expiryLabel(expiresOn(date, hour), now = now, zone = zone, locale = Locale.US)

    private val today = LocalDate.of(2026, 10, 11)

    @Test
    fun `expiring today is critical`() {
        val result = label(today, hour = 23)
        assertEquals("Expires today", result.text)
        assertEquals(ExpiryUrgency.Expired, result.urgency)
    }

    @Test
    fun `expiring tomorrow is soon even when less than 24 hours away`() {
        val result = label(today.plusDays(1), hour = 9)
        assertEquals("Expires tomorrow", result.text)
        assertEquals(ExpiryUrgency.Soon, result.urgency)
    }

    @Test
    fun `within a week is soon`() {
        val result = label(today.plusDays(7))
        assertEquals("7 days left", result.text)
        assertEquals(ExpiryUrgency.Soon, result.urgency)
    }

    @Test
    fun `eight to thirty days is normal with a day count`() {
        val result = label(today.plusDays(30))
        assertEquals("30 days left", result.text)
        assertEquals(ExpiryUrgency.Normal, result.urgency)
    }

    @Test
    fun `beyond thirty days shows the date`() {
        val result = label(LocalDate.of(2027, 3, 14))
        assertEquals("Until 14 Mar", result.text)
        assertEquals(ExpiryUrgency.Normal, result.urgency)
    }

    @Test
    fun `beyond thirty days in another year still shows day and month`() {
        assertEquals("Until 2 Jan", label(LocalDate.of(2027, 1, 2)).text)
    }

    @Test
    fun `yesterday reads as expired one day ago`() {
        val result = label(today.minusDays(1))
        assertEquals("Expired 1 day ago", result.text)
        assertEquals(ExpiryUrgency.Expired, result.urgency)
    }

    @Test
    fun `older expiry counts days`() {
        assertEquals("Expired 3 days ago", label(today.minusDays(3)).text)
    }

    @Test
    fun `days left is reported as a signed calendar day count`() {
        assertEquals(-3L, label(today.minusDays(3)).daysLeft)
        assertEquals(12L, label(today.plusDays(12)).daysLeft)
    }

    @Test
    fun `money uses two decimals and grouping`() {
        assertEquals("₪1,234.50", formatMoney(1234.5, "₪", Locale.US))
    }

    @Test
    fun `money rounds half up to cents`() {
        assertEquals("$0.13", formatMoney(0.125, "$", Locale.US))
    }

    @Test
    fun `negative zero balance shows as zero`() {
        assertEquals("₪0.00", formatMoney(-0.0000001, "₪", Locale.US))
    }

    @Test
    fun `short money drops cents for whole amounts`() {
        assertEquals("₪250", formatMoneyShort(250.0, "₪", Locale.US))
        assertEquals("₪35.90", formatMoneyShort(35.9, "₪", Locale.US))
    }

    @Test
    fun `parse amount accepts a comma decimal separator`() {
        assertEquals(12.5, parseAmount("12,5")!!, 0.0001)
    }

    @Test
    fun `parse amount rejects blank and non numbers`() {
        assertEquals(null, parseAmount(""))
        assertEquals(null, parseAmount("abc"))
    }
}
