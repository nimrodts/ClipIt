package com.nimroddayan.clipit.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nimroddayan.clipit.data.model.Coupon
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [CouponDao]. These tests run on an Android device/emulator with an
 * in-memory database.
 */
@RunWith(AndroidJUnit4::class)
class CouponDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var couponDao: CouponDao

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
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun createTestCoupon(
            name: String = "Test Coupon",
            currentValue: Double = 100.0,
            initialValue: Double = 100.0,
            categoryId: Long? = null,
            redeemCode: String? = null,
            isArchived: Boolean = false,
            isPending: Boolean = false
    ) =
            Coupon(
                    name = name,
                    currentValue = currentValue,
                    initialValue = initialValue,
                    expirationDate = System.currentTimeMillis() + 86400000L, // Tomorrow
                    categoryId = categoryId,
                    redeemCode = redeemCode,
                    isArchived = isArchived,
                    isPending = isPending
            )

    @Test
    fun insertAndRetrieveCoupon() = runTest {
        val coupon = createTestCoupon(name = "Gift Card")
        val ids = couponDao.insertAll(coupon)
        val insertedId = ids.first()

        val retrieved = couponDao.getCouponById(insertedId)

        assertNotNull(retrieved)
        assertEquals("Gift Card", retrieved?.name)
        assertEquals(100.0, retrieved?.currentValue ?: 0.0, 0.01)
    }

    @Test
    fun updateCouponBalance() = runTest {
        val coupon = createTestCoupon(currentValue = 100.0)
        val ids = couponDao.insertAll(coupon)
        val insertedId = ids.first()

        val insertedCoupon = couponDao.getCouponById(insertedId)!!
        val updatedCoupon = insertedCoupon.copy(currentValue = 50.0)
        couponDao.update(updatedCoupon)

        val retrieved = couponDao.getCouponById(insertedId)
        assertEquals(50.0, retrieved?.currentValue ?: 0.0, 0.01)
    }

    @Test
    fun deleteCoupon() = runTest {
        val coupon = createTestCoupon()
        val ids = couponDao.insertAll(coupon)
        val insertedId = ids.first()

        val insertedCoupon = couponDao.getCouponById(insertedId)!!
        couponDao.delete(insertedCoupon)

        val retrieved = couponDao.getCouponById(insertedId)
        assertNull(retrieved)
    }

    @Test
    fun getAllReturnsOnlyActiveNonPendingCoupons() = runTest {
        couponDao.insertAll(createTestCoupon(name = "Active"))
        couponDao.insertAll(createTestCoupon(name = "Archived", isArchived = true))
        couponDao.insertAll(createTestCoupon(name = "Pending", isPending = true))

        val activeCoupons = couponDao.getAll().first()

        assertEquals(1, activeCoupons.size)
        assertEquals("Active", activeCoupons[0].name)
    }

    @Test
    fun getArchivedReturnsOnlyArchivedCoupons() = runTest {
        couponDao.insertAll(createTestCoupon(name = "Active"))
        couponDao.insertAll(createTestCoupon(name = "Archived", isArchived = true))

        val archivedCoupons = couponDao.getArchived().first()

        assertEquals(1, archivedCoupons.size)
        assertEquals("Archived", archivedCoupons[0].name)
    }

    @Test
    fun getPendingCouponsReturnsOnlyPendingCoupons() = runTest {
        couponDao.insertAll(createTestCoupon(name = "Active"))
        couponDao.insertAll(createTestCoupon(name = "Pending", isPending = true))

        val pendingCoupons = couponDao.getPendingCoupons().first()

        assertEquals(1, pendingCoupons.size)
        assertEquals("Pending", pendingCoupons[0].name)
    }

    @Test
    fun getCouponByRedeemCodeReturnsCorrectCoupon() = runTest {
        couponDao.insertAll(createTestCoupon(name = "Gift Card", redeemCode = "ABCD-1234"))
        couponDao.insertAll(createTestCoupon(name = "Other", redeemCode = "WXYZ-5678"))

        val found = couponDao.getCouponByRedeemCode("ABCD-1234")

        assertNotNull(found)
        assertEquals("Gift Card", found?.name)
    }

    @Test
    fun getCouponByRedeemCodeReturnsNullForNotFound() = runTest {
        couponDao.insertAll(createTestCoupon(redeemCode = "ABCD-1234"))

        val found = couponDao.getCouponByRedeemCode("NONEXISTENT")

        assertNull(found)
    }

    @Test
    fun clearAllRemovesAllCoupons() = runTest {
        couponDao.insertAll(createTestCoupon(name = "Coupon 1"))
        couponDao.insertAll(createTestCoupon(name = "Coupon 2"))
        couponDao.insertAll(createTestCoupon(name = "Coupon 3"))

        couponDao.clearAll()

        val allCoupons = couponDao.getAll().first()
        assertTrue(allCoupons.isEmpty())
    }

    // ========== Edge Cases and Error Handling ==========

    @Test
    fun getCouponById_returnsNullForNonexistentId() = runTest {
        val result = couponDao.getCouponById(999999L)
        assertNull(result)
    }

    @Test
    fun getCouponByRedeemCode_returnsNullForNonexistentCode() = runTest {
        couponDao.insertAll(createTestCoupon(redeemCode = "EXISTING-CODE"))

        val result = couponDao.getCouponByRedeemCode("NONEXISTENT-CODE")
        assertNull(result)
    }

    @Test
    fun insertCouponWithZeroValue() = runTest {
        val coupon = createTestCoupon(currentValue = 0.0, initialValue = 0.0)
        val ids = couponDao.insertAll(coupon)

        val retrieved = couponDao.getCouponById(ids.first())
        assertEquals(0.0, retrieved?.currentValue ?: -1.0, 0.01)
    }

    @Test
    fun insertCouponWithNegativeValue() = runTest {
        // While logically invalid, test that DB handles it
        val coupon = createTestCoupon(currentValue = -50.0, initialValue = 100.0)
        val ids = couponDao.insertAll(coupon)

        val retrieved = couponDao.getCouponById(ids.first())
        assertEquals(-50.0, retrieved?.currentValue ?: 0.0, 0.01)
    }

    @Test
    fun insertCouponWithVeryLargeValue() = runTest {
        val largeValue = 999999999.99
        val coupon = createTestCoupon(currentValue = largeValue, initialValue = largeValue)
        val ids = couponDao.insertAll(coupon)

        val retrieved = couponDao.getCouponById(ids.first())
        assertEquals(largeValue, retrieved?.currentValue ?: 0.0, 0.01)
    }

    @Test
    fun insertCouponWithEmptyName() = runTest {
        val coupon = createTestCoupon(name = "")
        val ids = couponDao.insertAll(coupon)

        val retrieved = couponDao.getCouponById(ids.first())
        assertEquals("", retrieved?.name)
    }

    @Test
    fun insertCouponWithVeryLongName() = runTest {
        val longName = "A".repeat(1000)
        val coupon = createTestCoupon(name = longName)
        val ids = couponDao.insertAll(coupon)

        val retrieved = couponDao.getCouponById(ids.first())
        assertEquals(longName, retrieved?.name)
    }

    @Test
    fun insertCouponWithSpecialCharactersInName() = runTest {
        val specialName = "קופון 🎁 50% Off! @#\$%"
        val coupon = createTestCoupon(name = specialName)
        val ids = couponDao.insertAll(coupon)

        val retrieved = couponDao.getCouponById(ids.first())
        assertEquals(specialName, retrieved?.name)
    }

    @Test
    fun updateNonexistentCoupon_doesNotThrow() = runTest {
        val fakeCoupon =
                Coupon(
                        id = 999999L,
                        name = "Fake",
                        currentValue = 100.0,
                        initialValue = 100.0,
                        expirationDate = System.currentTimeMillis(),
                        categoryId = null
                )

        // Should not throw, just update 0 rows
        couponDao.update(fakeCoupon)

        // Verify DB is still in good state
        val all = couponDao.getAll().first()
        assertTrue(all.isEmpty())
    }

    @Test
    fun deleteNonexistentCoupon_doesNotThrow() = runTest {
        val fakeCoupon =
                Coupon(
                        id = 999999L,
                        name = "Fake",
                        currentValue = 100.0,
                        initialValue = 100.0,
                        expirationDate = System.currentTimeMillis(),
                        categoryId = null
                )

        // Should not throw, just delete 0 rows
        couponDao.delete(fakeCoupon)
    }

    @Test
    fun multipleCouponsWithSameNameAllowed() = runTest {
        couponDao.insertAll(createTestCoupon(name = "Same Name"))
        couponDao.insertAll(createTestCoupon(name = "Same Name"))
        couponDao.insertAll(createTestCoupon(name = "Same Name"))

        val coupons = couponDao.getAll().first()
        assertEquals(3, coupons.size)
    }
}
