package com.nimroddayan.clipit.data.ai

import com.nimroddayan.clipit.data.gemini.ParsedCoupon
import com.nimroddayan.clipit.data.model.Coupon
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class CouponAiRetrierTest {

    private val sms = "Fox gift card 1234-5678, 100 NIS"
    private val aiResult = ParsedCoupon("Fox", 100.0, "2027-01-31", "Fox gift card", "1234-5678", null)
    private val failed =
        Coupon(
            id = 7,
            name = "New Coupon",
            currentValue = 100.0,
            initialValue = 100.0,
            expirationDate = 0L,
            categoryId = null,
            redeemCode = "1234-5678",
            isPending = true,
            sourceSms = sms,
            sourceSender = "BuyMe",
            aiStatus = AiStatus.FAILED_RETRYING,
            aiError = ParsedCoupon.ERROR_TRANSIENT,
        )

    private class FakeStore(vararg coupons: Coupon) : PendingCouponStore {
        val coupons = coupons.associateBy { it.id }.toMutableMap()
        val saved = mutableListOf<Coupon>()
        /** Runs between the AI call and the write, to simulate the user editing meanwhile. */
        var onSecondGet: (() -> Unit)? = null
        private var gets = 0

        override suspend fun get(id: Long): Coupon? {
            if (++gets == 2) onSecondGet?.invoke()
            return coupons[id]
        }

        override suspend fun findByRedeemCode(code: String): Coupon? =
            coupons.values.firstOrNull { it.redeemCode == code }

        override suspend fun save(coupon: Coupon) {
            saved += coupon
            coupons[coupon.id] = coupon
        }
    }

    private fun retrier(store: FakeStore, result: ParsedCoupon = aiResult, onParse: (String) -> Unit = {}) =
        CouponAiRetrier(parse = { onParse(it); result }, store = store, now = { 1_000L })

    @Test
    fun `success replaces the draft with the ai result and clears the failure`() = runTest {
        val store = FakeStore(failed)
        var parsedText: String? = null
        val outcome = retrier(store, onParse = { parsedText = it }).retryInBackground(7, isLastAttempt = false)

        assertEquals(sms, parsedText)
        val updated = (outcome as RetryOutcome.Updated).coupon
        assertEquals(7L, updated.id)
        assertEquals("Fox", updated.name)
        assertTrue(updated.isPending)
        assertNull(updated.aiStatus)
        assertNull(updated.aiError)
        assertEquals(sms, updated.sourceSms)
        assertEquals("BuyMe", updated.sourceSender)
        assertEquals(listOf(updated), store.saved)
    }

    @Test
    fun `success applies the ai category but keeps one the user already set`() = runTest {
        val picked = aiResult.copy(categoryId = 4)

        val fresh = retrier(FakeStore(failed), picked).retryInBackground(7, false)
        val userSet = retrier(FakeStore(failed.copy(categoryId = 9)), picked).retryInBackground(7, false)

        assertEquals(4L, (fresh as RetryOutcome.Updated).coupon.categoryId)
        assertEquals(9L, (userSet as RetryOutcome.Updated).coupon.categoryId)
    }

    @Test
    fun `transient failure keeps retrying and records the latest error`() = runTest {
        val store = FakeStore(failed.copy(aiError = "old"))
        val outcome = retrier(store, aiResult.copy(error = ParsedCoupon.ERROR_QUOTA)).retryInBackground(7, false)

        assertEquals(RetryOutcome.StillFailing(ParsedCoupon.ERROR_QUOTA), outcome)
        assertEquals(AiStatus.FAILED_RETRYING, store.coupons[7]!!.aiStatus)
        assertEquals(ParsedCoupon.ERROR_QUOTA, store.coupons[7]!!.aiError)
    }

    @Test
    fun `failure on the last attempt gives up`() = runTest {
        val store = FakeStore(failed)
        val outcome = retrier(store, aiResult.copy(error = ParsedCoupon.ERROR_TRANSIENT)).retryInBackground(7, true)

        assertEquals(RetryOutcome.Failed(ParsedCoupon.ERROR_TRANSIENT), outcome)
        assertEquals(AiStatus.FAILED, store.coupons[7]!!.aiStatus)
    }

    @Test
    fun `non transient failure stops background retries`() = runTest {
        val store = FakeStore(failed)
        val outcome = retrier(store, aiResult.copy(error = ParsedCoupon.ERROR_INVALID_KEY)).retryInBackground(7, false)

        assertEquals(RetryOutcome.Failed(ParsedCoupon.ERROR_INVALID_KEY), outcome)
        assertEquals(AiStatus.FAILED, store.coupons[7]!!.aiStatus)
    }

    @Test
    fun `background retry skips deleted approved or already fixed coupons`() = runTest {
        for (store in listOf(
            FakeStore(),
            FakeStore(failed.copy(isPending = false)),
            FakeStore(failed.copy(aiStatus = null)),
            FakeStore(failed.copy(aiStatus = AiStatus.FAILED)),
            FakeStore(failed.copy(sourceSms = null)),
        )) {
            var parsed = false
            val outcome = retrier(store, onParse = { parsed = true }).retryInBackground(7, false)
            assertSame(RetryOutcome.Skipped, outcome)
            assertEquals(false, parsed)
            assertTrue(store.saved.isEmpty())
        }
    }

    @Test
    fun `result is dropped when the user changed the coupon during the ai call`() = runTest {
        val store = FakeStore(failed)
        store.onSecondGet = { store.coupons[7] = failed.copy(isPending = false, name = "Mine") }
        val outcome = retrier(store).retryInBackground(7, false)

        assertSame(RetryOutcome.Skipped, outcome)
        assertTrue(store.saved.isEmpty())
        assertEquals("Mine", store.coupons[7]!!.name)
    }

    @Test
    fun `manual retry works on a coupon that already gave up`() = runTest {
        val store = FakeStore(failed.copy(aiStatus = AiStatus.FAILED))
        val outcome = retrier(store).retryNow(7)

        assertNull((outcome as RetryOutcome.Updated).coupon.aiStatus)
    }

    @Test
    fun `manual failure keeps background retries running`() = runTest {
        val store = FakeStore(failed)
        val outcome = retrier(store, aiResult.copy(error = ParsedCoupon.ERROR_TRANSIENT)).retryNow(7)

        assertEquals(RetryOutcome.StillFailing(ParsedCoupon.ERROR_TRANSIENT), outcome)
        assertEquals(AiStatus.FAILED_RETRYING, store.coupons[7]!!.aiStatus)
    }

    @Test
    fun `manual failure on a given up coupon stays failed`() = runTest {
        val store = FakeStore(failed.copy(aiStatus = AiStatus.FAILED))
        val outcome = retrier(store, aiResult.copy(error = ParsedCoupon.ERROR_TRANSIENT)).retryNow(7)

        assertEquals(RetryOutcome.Failed(ParsedCoupon.ERROR_TRANSIENT), outcome)
        assertEquals(AiStatus.FAILED, store.coupons[7]!!.aiStatus)
    }

    @Test
    fun `redeem code owned by another coupon marks the draft as a duplicate`() = runTest {
        val other = failed.copy(id = 3, isPending = false, aiStatus = null, redeemCode = "1234-5678")
        val store = FakeStore(failed.copy(redeemCode = "REVIEW-1"), other)
        val outcome = retrier(store).retryInBackground(7, false)

        assertEquals(RetryOutcome.Failed(CouponAiRetrier.ERROR_DUPLICATE), outcome)
        assertEquals(AiStatus.FAILED, store.coupons[7]!!.aiStatus)
        assertEquals("REVIEW-1", store.coupons[7]!!.redeemCode)
    }
}
