package com.nimroddayan.clipit.data.repo

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nimroddayan.clipit.data.db.AppDatabase
import com.nimroddayan.clipit.data.db.CouponDao
import com.nimroddayan.clipit.data.db.CouponHistoryDao
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

/** Instrumented tests for [CouponHistoryRepository]. */
@RunWith(AndroidJUnit4::class)
class CouponHistoryRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var couponDao: CouponDao
    private lateinit var couponHistoryDao: CouponHistoryDao
    private lateinit var repository: CouponHistoryRepository

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
        repository = CouponHistoryRepository(couponHistoryDao)
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
    fun getHistoryForCoupon_returnsFlowOfHistory() = runTest {
        val couponId = createTestCouponAndGetId()

        // Add history entries directly via DAO
        couponHistoryDao.insert(
                CouponHistory(couponId = couponId, action = "Action1", changeSummary = "Summary1")
        )
        couponHistoryDao.insert(
                CouponHistory(couponId = couponId, action = "Action2", changeSummary = "Summary2")
        )

        val history = repository.getHistoryForCoupon(couponId).first()

        assertEquals(2, history.size)
    }

    @Test
    fun getHistoryForCoupon_returnsEmptyForNewCoupon() = runTest {
        val couponId = createTestCouponAndGetId()

        val history = repository.getHistoryForCoupon(couponId).first()

        assertTrue(history.isEmpty())
    }

    @Test
    fun addHistoryEntry_insertsHistory() = runTest {
        val couponId = createTestCouponAndGetId()
        val history =
                CouponHistory(
                        couponId = couponId,
                        action = "Test Action",
                        changeSummary = "Test Summary"
                )

        repository.addHistoryEntry(history)

        val histories = couponHistoryDao.getHistoryForCoupon(couponId).first()
        assertEquals(1, histories.size)
        assertEquals("Test Action", histories[0].action)
        assertEquals("Test Summary", histories[0].changeSummary)
    }

    @Test
    fun addHistoryEntry_multipleEntries() = runTest {
        val couponId = createTestCouponAndGetId()

        repository.addHistoryEntry(
                CouponHistory(couponId = couponId, action = "A1", changeSummary = "S1")
        )
        repository.addHistoryEntry(
                CouponHistory(couponId = couponId, action = "A2", changeSummary = "S2")
        )
        repository.addHistoryEntry(
                CouponHistory(couponId = couponId, action = "A3", changeSummary = "S3")
        )

        val histories = repository.getHistoryForCoupon(couponId).first()
        assertEquals(3, histories.size)
    }
}
