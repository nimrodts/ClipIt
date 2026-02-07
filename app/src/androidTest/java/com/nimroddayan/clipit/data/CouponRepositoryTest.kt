package com.nimroddayan.clipit.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nimroddayan.clipit.data.db.AppDatabase
import com.nimroddayan.clipit.data.db.CouponDao
import com.nimroddayan.clipit.data.db.CouponHistoryDao
import com.nimroddayan.clipit.data.model.Coupon
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [CouponRepository]. Tests business logic including duplicate detection,
 * coupon usage, and undo functionality.
 */
@RunWith(AndroidJUnit4::class)
class CouponRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var couponDao: CouponDao
    private lateinit var couponHistoryDao: CouponHistoryDao
    private lateinit var repository: CouponRepository

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
        repository = CouponRepository(couponDao, couponHistoryDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun createTestCoupon(
            name: String = "Test Coupon",
            currentValue: Double = 100.0,
            initialValue: Double = 100.0,
            redeemCode: String? = null
    ) =
            Coupon(
                    name = name,
                    currentValue = currentValue,
                    initialValue = initialValue,
                    expirationDate = System.currentTimeMillis() + 86400000L,
                    categoryId = null,
                    redeemCode = redeemCode
            )

    // ========== Insert Tests ==========

    @Test
    fun insert_createsHistoryEntry() = runTest {
        val coupon = createTestCoupon()
        repository.insert(coupon)

        val coupons = couponDao.getAll().first()
        assertEquals(1, coupons.size)

        val history = couponHistoryDao.getHistoryForCoupon(coupons[0].id).first()
        assertEquals(1, history.size)
        assertEquals("Coupon Created", history[0].action)
    }

    @Test(expected = DuplicateRedeemCodeException::class)
    fun insert_throwsExceptionForDuplicateRedeemCode() = runTest {
        val coupon1 = createTestCoupon(name = "Coupon 1", redeemCode = "DUPLICATE-CODE")
        val coupon2 = createTestCoupon(name = "Coupon 2", redeemCode = "DUPLICATE-CODE")

        repository.insert(coupon1)
        repository.insert(coupon2) // Should throw
    }

    @Test
    fun insert_allowsSameRedeemCodeIfBlank() = runTest {
        val coupon1 = createTestCoupon(name = "Coupon 1", redeemCode = "")
        val coupon2 = createTestCoupon(name = "Coupon 2", redeemCode = "")

        repository.insert(coupon1)
        repository.insert(coupon2) // Should NOT throw

        val coupons = couponDao.getAll().first()
        assertEquals(2, coupons.size)
    }

    @Test
    fun insert_allowsNullRedeemCodes() = runTest {
        val coupon1 = createTestCoupon(name = "Coupon 1", redeemCode = null)
        val coupon2 = createTestCoupon(name = "Coupon 2", redeemCode = null)

        repository.insert(coupon1)
        repository.insert(coupon2) // Should NOT throw

        val coupons = couponDao.getAll().first()
        assertEquals(2, coupons.size)
    }

    // ========== Use Coupon Tests ==========

    @Test
    fun use_reducesBalance() = runTest {
        val coupon = createTestCoupon(currentValue = 100.0)
        repository.insert(coupon)
        val insertedCoupon = couponDao.getAll().first().first()

        repository.use(insertedCoupon, 30.0)

        val updatedCoupon = couponDao.getCouponById(insertedCoupon.id)
        assertEquals(70.0, updatedCoupon?.currentValue ?: 0.0, 0.01)
    }

    @Test
    fun use_createsHistoryEntry() = runTest {
        val coupon = createTestCoupon(currentValue = 100.0)
        repository.insert(coupon)
        val insertedCoupon = couponDao.getAll().first().first()

        repository.use(insertedCoupon, 25.0)

        val history = couponHistoryDao.getHistoryForCoupon(insertedCoupon.id).first()
        val useEntry = history.find { it.action == "Coupon Used" }
        assertNotNull(useEntry)
        assertEquals("25.0", useEntry?.changeSummary)
    }

    @Test
    fun use_archivesCouponWhenFullyUsed() = runTest {
        val coupon = createTestCoupon(currentValue = 50.0)
        repository.insert(coupon)
        val insertedCoupon = couponDao.getAll().first().first()

        repository.use(insertedCoupon, 50.0) // Use entire balance

        val updatedCoupon = couponDao.getCouponById(insertedCoupon.id)
        assertTrue(updatedCoupon?.isArchived ?: false)
    }

    @Test
    fun use_archivesCouponWhenOverdrawn() = runTest {
        val coupon = createTestCoupon(currentValue = 50.0)
        repository.insert(coupon)
        val insertedCoupon = couponDao.getAll().first().first()

        repository.use(insertedCoupon, 75.0) // More than available

        val updatedCoupon = couponDao.getCouponById(insertedCoupon.id)
        assertTrue(updatedCoupon?.isArchived ?: false)
        assertEquals(-25.0, updatedCoupon?.currentValue ?: 0.0, 0.01)
    }

    // ========== Archive/Unarchive Tests ==========

    @Test
    fun archive_setsArchivedFlag() = runTest {
        val coupon = createTestCoupon()
        repository.insert(coupon)
        val insertedCoupon = couponDao.getAll().first().first()

        repository.archive(insertedCoupon)

        val archivedCoupon = couponDao.getCouponById(insertedCoupon.id)
        assertTrue(archivedCoupon?.isArchived ?: false)
    }

    @Test
    fun unarchive_clearsArchivedFlag() = runTest {
        val coupon = createTestCoupon()
        repository.insert(coupon)
        val insertedCoupon = couponDao.getAll().first().first()
        repository.archive(insertedCoupon)

        val archivedCoupon = couponDao.getCouponById(insertedCoupon.id)!!
        repository.unarchive(archivedCoupon)

        val unarchivedCoupon = couponDao.getCouponById(insertedCoupon.id)
        assertTrue(!(unarchivedCoupon?.isArchived ?: true))
    }

    // ========== Pending Coupon Tests ==========

    @Test
    fun approveCoupon_clearsPendingFlag() = runTest {
        val coupon = createTestCoupon().copy(isPending = true)
        couponDao.insertAll(coupon)
        val pendingCoupon = couponDao.getPendingCoupons().first().first()

        repository.approveCoupon(pendingCoupon)

        val approvedCoupon = couponDao.getCouponById(pendingCoupon.id)
        assertTrue(!(approvedCoupon?.isPending ?: true))
    }

    @Test
    fun deletePendingCoupon_removesCoupon() = runTest {
        val coupon = createTestCoupon().copy(isPending = true)
        couponDao.insertAll(coupon)
        val pendingCoupon = couponDao.getPendingCoupons().first().first()

        repository.deletePendingCoupon(pendingCoupon)

        val allCoupons = couponDao.getAll().first()
        val pendingCoupons = couponDao.getPendingCoupons().first()
        assertTrue(allCoupons.isEmpty())
        assertTrue(pendingCoupons.isEmpty())
    }
}
