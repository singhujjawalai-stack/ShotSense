package com.singhujjawalai.shotsense

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenshotClassifierTest {

    @Test
    fun `upi payment with rupee amount classifies as bill`() {
        val text = """
            Paid to Swiggy
            ₹249.00
            Transaction successful
            UPI Ref: 123456789012
        """.trimIndent()
        val result = ScreenshotClassifier.classify(text)
        assertEquals(ScreenshotClassifier.CATEGORY_BILL, result.category)
        assertTrue(result.amounts.any { it.contains("249") })
    }

    @Test
    fun `debited rupee amount classifies as bill`() {
        val result = ScreenshotClassifier.classify("₹1,500 debited from A/c XX1234")
        assertEquals(ScreenshotClassifier.CATEGORY_BILL, result.category)
        assertEquals("₹1,500", result.amounts.first())
    }

    @Test
    fun `rupee amount with no space still captured`() {
        val result = ScreenshotClassifier.classify("Total: ₹17,70,000 credited")
        assertEquals(ScreenshotClassifier.CATEGORY_BILL, result.category)
        assertEquals("₹17,70,000", result.amounts.first())
    }

    @Test
    fun `otp still wins over amounts`() {
        val result = ScreenshotClassifier.classify("Your OTP is 123456. Do not share. Txn of ₹99")
        assertEquals(ScreenshotClassifier.CATEGORY_OTP, result.category)
    }

    @Test
    fun `utr without amount stays code`() {
        val result = ScreenshotClassifier.classify("UPI Ref 123456789012 done")
        assertEquals(ScreenshotClassifier.CATEGORY_CODE, result.category)
    }

    @Test
    fun `plain text without amounts stays text`() {
        val result = ScreenshotClassifier.classify("Meeting notes from today's sync about the roadmap")
        assertEquals(ScreenshotClassifier.CATEGORY_TEXT, result.category)
    }
}
