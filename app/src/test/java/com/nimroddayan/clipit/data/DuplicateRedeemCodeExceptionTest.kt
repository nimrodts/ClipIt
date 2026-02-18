package com.nimroddayan.clipit.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/** Unit tests for [DuplicateRedeemCodeException]. */
class DuplicateRedeemCodeExceptionTest {

    @Test
    fun `exception has correct message`() {
        val message = "Redeem code ABC123 already exists"
        val exception = DuplicateRedeemCodeException(message)

        assertEquals(message, exception.message)
    }

    @Test
    fun `exception is throwable`() {
        val exception = DuplicateRedeemCodeException("Test")

        // Verify it's a proper Exception
        assert(exception is Exception)
    }

    @Test
    fun `different messages create different exceptions`() {
        val e1 = DuplicateRedeemCodeException("Message 1")
        val e2 = DuplicateRedeemCodeException("Message 2")

        assertNotEquals(e1.message, e2.message)
    }
}
