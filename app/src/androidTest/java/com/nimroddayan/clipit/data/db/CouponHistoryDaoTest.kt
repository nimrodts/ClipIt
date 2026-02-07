package com.nimroddayan.clipit.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nimroddayan.clipit.data.model.Coupon
import com.nimroddayan.clipit.data.model.CouponHistory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [CouponHistoryDao]. Tests run on an Android device/emulator with an
 * in-memory database.
 */
@RunWith(AndroidJUnit4::class)
class CouponHistoryDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var couponDao: CouponDao
    private lateinit var couponHistoryDao: CouponHistoryDao

    @Before
    fun setUp() {
        database =
                Room.inMemoryDatabaseBuilder(
                                ApplicationProvider.getApplicationContext(),
                                AppDatabase::class.java
                        )
                        .allowMainThreadQueries()
                        .build()
        couponDao = database.couponDao()
        couponHistoryDao = database.couponHistoryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private suspend fun createTestCouponAndGetId(): Long {
        val coupon =
                Coupon(
                        name = "Test Coupon",
                        currentValue = 100.0,
                        initialValue = 100.0,
                        expirationDate = System.currentTimeMillis() + 86400000L,
                        categoryId = null
                )
        val ids = couponDao.insertAll(coupon)
        return ids.first()
    }

    @Test
    fun insertAndRetrieveHistory() = runTest {
        val couponId = createTestCouponAndGetId()
        val history =
                CouponHistory(
                        couponId = couponId,
                        action = "Coupon Created",
                        changeSummary = "Test creation"
                )
        couponHistoryDao.insert(history)

        val histories = couponHistoryDao.getHistoryForCoupon(couponId).first()

        assertEquals(1, histories.size)
        assertEquals("Coupon Created", histories[0].action)
    }

    @Test
    fun getHistoryForCoupon_returnsOnlyThatCouponsHistory() = runTest {
        val couponId1 = createTestCouponAndGetId()
        val couponId2 = createTestCouponAndGetId()

        couponHistoryDao.insert(
                CouponHistory(couponId = couponId1, action = "Action1", changeSummary = "Summary1")
        )
        couponHistoryDao.insert(
                CouponHistory(couponId = couponId2, action = "Action2", changeSummary = "Summary2")
        )
        couponHistoryDao.insert(
                CouponHistory(couponId = couponId1, action = "Action3", changeSummary = "Summary3")
        )

        val coupon1History = couponHistoryDao.getHistoryForCoupon(couponId1).first()
        val coupon2History = couponHistoryDao.getHistoryForCoupon(couponId2).first()

        assertEquals(2, coupon1History.size)
        assertEquals(1, coupon2History.size)
    }

    @Test
    fun historyOrderedByTimestampDescending() = runTest {
        val couponId = createTestCouponAndGetId()

        // Insert with different timestamps
        couponHistoryDao.insert(
                CouponHistory(
                        couponId = couponId,
                        action = "First",
                        changeSummary = "",
                        timestamp = 1000L
                )
        )
        couponHistoryDao.insert(
                CouponHistory(
                        couponId = couponId,
                        action = "Second",
                        changeSummary = "",
                        timestamp = 2000L
                )
        )
        couponHistoryDao.insert(
                CouponHistory(
                        couponId = couponId,
                        action = "Third",
                        changeSummary = "",
                        timestamp = 3000L
                )
        )

        val histories = couponHistoryDao.getHistoryForCoupon(couponId).first()

        // Should be in descending order (newest first)
        assertEquals("Third", histories[0].action)
        assertEquals("Second", histories[1].action)
        assertEquals("First", histories[2].action)
    }

    @Test
    fun deleteHistory() = runTest {
        val couponId = createTestCouponAndGetId()
        val history =
                CouponHistory(couponId = couponId, action = "Test Action", changeSummary = "Test")
        couponHistoryDao.insert(history)

        val inserted = couponHistoryDao.getHistoryForCoupon(couponId).first().first()
        couponHistoryDao.delete(inserted)

        val histories = couponHistoryDao.getHistoryForCoupon(couponId).first()
        assertTrue(histories.isEmpty())
    }

    @Test
    fun historyDeletedWhenCouponDeleted_cascadeDelete() = runTest {
        val couponId = createTestCouponAndGetId()

        couponHistoryDao.insert(
                CouponHistory(couponId = couponId, action = "Action1", changeSummary = "Summary1")
        )
        couponHistoryDao.insert(
                CouponHistory(couponId = couponId, action = "Action2", changeSummary = "Summary2")
        )

        // Verify history exists
        var histories = couponHistoryDao.getHistoryForCoupon(couponId).first()
        assertEquals(2, histories.size)

        // Delete the coupon
        val coupon = couponDao.getCouponById(couponId)!!
        couponDao.delete(coupon)

        // History should be cascade deleted
        histories = couponHistoryDao.getHistoryForCoupon(couponId).first()
        assertTrue(histories.isEmpty())
    }

    @Test
    fun getAll_returnsAllHistories() = runTest {
        val couponId1 = createTestCouponAndGetId()
        val couponId2 = createTestCouponAndGetId()

        couponHistoryDao.insert(
                CouponHistory(couponId = couponId1, action = "Action1", changeSummary = "Summary1")
        )
        couponHistoryDao.insert(
                CouponHistory(couponId = couponId2, action = "Action2", changeSummary = "Summary2")
        )
        couponHistoryDao.insert(
                CouponHistory(couponId = couponId1, action = "Action3", changeSummary = "Summary3")
        )

        val allHistories = couponHistoryDao.getAll().first()

        assertEquals(3, allHistories.size)
    }
}
