package com.singhujjawalai.shotsense

/**
 * Rule-based classification of screenshot OCR text. Runs fully on-device;
 * only category labels and extracted identifiers are persisted, never the
 * full text beyond a short sample.
 */
object ScreenshotClassifier {
    // Bump when classification rules change; the service re-derives categories
    // for stored entries whose version is older (no re-OCR needed).
    const val CLS_VERSION = 2
    const val CATEGORY_BILL = "BILL"
    const val CATEGORY_OTP = "OTP"
    const val CATEGORY_CODE = "CODE"
    const val CATEGORY_TEXT = "TEXT"
    const val CATEGORY_NONE = "NONE"

    data class Code(val type: String, val value: String)

    data class Result(
        val category: String,
        val codes: List<Code>,
        val amounts: List<String>,
        val merchant: String?,
    )

    private val OTP_KEYWORD =
        Regex("(?i)\\b(otp|one[- ]time (password|code)|verification code|security code|login code)\\b")
    private val OTP_VALUE = Regex("(?<!\\d)\\d{6}(?!\\d)")
    private val UPI_ID = Regex("\\b[a-zA-Z0-9._-]{2,}@[a-zA-Z]{2,}\\b")
    private val UTR_VALUE = Regex(
        "(?i)\\b(?:UTR|UPI[ /-]?Ref(?:erence)?(?: number)?|Ref No\\.?|Reference No\\.?|Transaction ID|Txn ID|IMPS(?:/NEFT)? Ref)\\b[:# ]*([A-Z0-9]{8,22})\\b"
    )
    private val AWB_VALUE = Regex(
        "(?i)\\b(?:AWB|tracking|consignment|waybill|docket)\\b(?: no\\.?| number| id)?[:# ]*([A-Z0-9]{8,16})\\b"
    )
    private val PNR_VALUE = Regex("(?i)\\bPNR\\b[:# ]*(\\d{10})\\b")
    private val ORDER_ID = Regex(
        "(?i)\\b(?:order|booking)[ _-]?(?:id|no\\.?|number)[:# ]*([A-Z0-9][A-Z0-9-]{5,23})\\b"
    )
    private val AMOUNT = Regex("(?:₹|Rs\\.?|INR)\\s?([0-9][0-9,]*(?:\\.[0-9]{1,2})?)")
    private val BILL_KEYWORD = Regex(
        "(?i)\\b(invoice|receipt|bill|paid|payment (successful|done|of)|order confirmed|order placed|total amount|amount paid|thanks for your (purchase|order)|subscription renewed|txn successful)\\b"
    )
    // Looser net so everyday payment screens ("Transaction successful",
    // "₹500 debited", "Sent via UPI") without invoice wording still file as bills.
    private val PAYMENT_CONTEXT = Regex(
        "(?i)\\b(paid|payment|debited|credited|sent|received|spent|txn|transaction|upi|neft|imps|recharge|cashback|refund|emi|due|purchase|renewed)\\b"
    )

    private val KNOWN_MERCHANTS = listOf(
        "Swiggy", "Zomato", "Zepto", "Blinkit", "BigBasket", "Amazon", "Flipkart", "Myntra",
        "Ajio", "Meesho", "Paytm", "PhonePe", "Google Pay", "GPay", "Uber", "Ola", "Rapido",
        "IRCTC", "MakeMyTrip", "IndiGo", "SpiceJet", "Air India", "Vistara", "BookMyShow",
        "Netflix", "Spotify", "YouTube", "Airtel", "Jio", "Vi", "Vodafone", "BSNL",
        "Domino's", "McDonald's", "KFC", "Starbucks", "CRED", "Navi", "Groww", "Zerodha",
    )

    fun classify(rawText: String): Result {
        val text = rawText.trim()
        if (text.length < 3) return Result(CATEGORY_NONE, emptyList(), emptyList(), null)

        val codes = LinkedHashSet<Code>()
        for (match in UPI_ID.findAll(text)) codes.add(Code("upi", match.value))
        for (match in PNR_VALUE.findAll(text)) codes.add(Code("pnr", match.groupValues[1]))
        for (match in UTR_VALUE.findAll(text)) codes.add(Code("utr", match.groupValues[1]))
        for (match in AWB_VALUE.findAll(text)) codes.add(Code("awb", match.groupValues[1]))
        for (match in ORDER_ID.findAll(text)) codes.add(Code("order", match.groupValues[1]))

        val amounts = AMOUNT.findAll(text).map { it.value }.distinct().take(5).toList()

        val hasOtpKeyword = OTP_KEYWORD.containsMatchIn(text)
        val otpValues = if (hasOtpKeyword) OTP_VALUE.findAll(text).take(2).toList() else emptyList()
        for (match in otpValues) codes.add(Code("otp", match.value))

        val looksLikeBill = amounts.isNotEmpty() &&
            (BILL_KEYWORD.containsMatchIn(text) || PAYMENT_CONTEXT.containsMatchIn(text))
        val hasIdentityCodes = codes.any { it.type != "otp" }

        val category = when {
            otpValues.isNotEmpty() -> CATEGORY_OTP
            looksLikeBill -> CATEGORY_BILL
            hasIdentityCodes -> CATEGORY_CODE
            text.length >= 8 -> CATEGORY_TEXT
            else -> CATEGORY_NONE
        }

        return Result(category, codes.toList(), amounts, guessMerchant(text))
    }

    private fun guessMerchant(text: String): String? {
        for (merchant in KNOWN_MERCHANTS) {
            if (Regex("\\b${Regex.escape(merchant)}\\b", RegexOption.IGNORE_CASE).containsMatchIn(text)) {
                return merchant
            }
        }
        return null
    }
}
