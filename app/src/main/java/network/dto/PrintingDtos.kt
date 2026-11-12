package com.example.stismartlib.network.dto

import com.google.gson.annotations.SerializedName

data class PrintServiceStatusDto(
    @SerializedName(value = "accepting_requests", alternate = ["acceptingRequests"])
    val acceptingRequests: Any? = null,
    @SerializedName(value = "unavailable_reason", alternate = ["unavailableReason"])
    val unavailableReason: String? = null,
    @SerializedName(value = "docx_auto_count_available", alternate = ["docxAutoCountAvailable"])
    val docxAutoCountAvailable: Boolean? = null,
) {
    fun isAccepting(): Boolean = when (acceptingRequests) {
        is Boolean -> acceptingRequests
        is Number -> acceptingRequests.toInt() != 0
        is String -> acceptingRequests.equals("true", ignoreCase = true) || acceptingRequests == "1"
        else -> false
    }
}

data class PrintQuoteDto(
    @SerializedName(value = "page_count", alternate = ["pageCount"])
    val pageCount: Int? = null,
    @SerializedName(value = "total_sheets", alternate = ["totalSheets"])
    val totalSheets: Int? = null,
    @SerializedName(value = "calculated_cost", alternate = ["calculatedCost"])
    val calculatedCost: Double? = null,
    @SerializedName(value = "document_sha256", alternate = ["documentSha256"])
    val documentSha256: String? = null,
    @SerializedName(value = "printable_file_name", alternate = ["printableFileName"])
    val printableFileName: String? = null,
)

data class PrintQuoteResult(
    val pageCount: Int,
    val totalSheets: Int,
    val calculatedCost: Double,
    val documentSha256: String,
    val printableFileName: String?,
)

data class PrintReceiptDto(
    @SerializedName(value = "print_receipt_id", alternate = ["printReceiptId"])
    val printReceiptId: Int? = null,
    @SerializedName(value = "request_id", alternate = ["requestId"])
    val requestId: Int? = null,
    @SerializedName(value = "receipt_number", alternate = ["receiptNumber"])
    val receiptNumber: String? = null,
    @SerializedName(value = "verification_code", alternate = ["verificationCode"])
    val verificationCode: String? = null,
    @SerializedName(value = "receipt_status", alternate = ["receiptStatus"])
    val receiptStatus: String? = null,
    @SerializedName(value = "student_name", alternate = ["studentName"])
    val studentName: String? = null,
    @SerializedName(value = "school_id", alternate = ["schoolId"])
    val schoolId: String? = null,
    @SerializedName(value = "file_name", alternate = ["fileName"])
    val fileName: String? = null,
    @SerializedName(value = "page_count", alternate = ["pageCount"])
    val pageCount: Int? = null,
    @SerializedName(value = "number_of_copies", alternate = ["numberOfCopies"])
    val numberOfCopies: Int? = null,
    @SerializedName(value = "total_sheets", alternate = ["totalSheets"])
    val totalSheets: Int? = null,
    @SerializedName(value = "print_type", alternate = ["printType"])
    val printType: String? = null,
    @SerializedName(value = "paper_size", alternate = ["paperSize"])
    val paperSize: String? = null,
    @SerializedName(value = "amount_received", alternate = ["amountReceived"])
    val amountReceived: Double? = null,
    @SerializedName(value = "payment_method", alternate = ["paymentMethod"])
    val paymentMethod: String? = null,
    @SerializedName(value = "received_by", alternate = ["receivedBy"])
    val receivedBy: String? = null,
    @SerializedName(value = "received_at", alternate = ["receivedAt"])
    val receivedAt: String? = null,
)
