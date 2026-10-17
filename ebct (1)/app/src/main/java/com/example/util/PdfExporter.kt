package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.BillCalculationResult
import com.example.data.model.BillRecord
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    private const val PAGE_WIDTH = 595 // A4 standard width in points
    private const val PAGE_HEIGHT = 842 // A4 standard height in points

    fun generateBillPdf(context: Context, result: BillCalculationResult): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Header background banner
        paint.color = Color.parseColor("#0B192C") // Brand Navy Dark
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 110f, paint)

        // Header accent line
        paint.color = Color.parseColor("#10B981") // Brand Green
        canvas.drawRect(0f, 106f, PAGE_WIDTH.toFloat(), 110f, paint)

        // Header Title
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 24f
        canvas.drawText("EBCT", 36f, 48f, paint)

        // Subtitle
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 12f
        paint.color = Color.parseColor("#A7F3D0")
        canvas.drawText("Electricity Bill Calculate Fast — Bill Statement", 36f, 68f, paint)

        // Statement Date & Tagline
        paint.textSize = 10f
        paint.color = Color.parseColor("#E2E8F0")
        val dateStr = SimpleDateFormat("dd-MMM-yyyy, hh:mm a", Locale.getDefault()).format(Date(result.calculationTimestamp))
        canvas.drawText("Generated on: $dateStr", 36f, 88f, paint)

        var y = 130f

        // 2. Consumer & Connection Summary Card
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        cardPaint.color = Color.parseColor("#F8FAFC")
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1f
        }

        val cardHeight = if (result.userProfile?.isConfigured == true) 130f else 115f
        val summaryRect = RectF(36f, y, (PAGE_WIDTH - 36).toFloat(), y + cardHeight)
        canvas.drawRoundRect(summaryRect, 10f, 10f, cardPaint)
        canvas.drawRoundRect(summaryRect, 10f, 10f, strokePaint)

        // Content inside Summary Card
        paint.color = Color.parseColor("#0B192C")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 13f
        canvas.drawText("CONNECTION & CONSUMER DETAILS", 52f, y + 24f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 10.5f
        paint.color = Color.parseColor("#475569")

        var textY = y + 44f
        if (result.userProfile?.isConfigured == true) {
            if (result.userProfile.name.isNotBlank()) {
                canvas.drawText("Consumer Name: ${result.userProfile.name}", 52f, textY, paint)
                textY += 16f
            }
            if (result.userProfile.builtNo.isNotBlank()) {
                canvas.drawText("Built No / Consumer No: ${result.userProfile.builtNo}", 52f, textY, paint)
                textY += 16f
            }
            if (result.userProfile.address.isNotBlank()) {
                canvas.drawText("Address: ${result.userProfile.address}", 52f, textY, paint)
                textY += 16f
            }
            if (result.userProfile.email.isNotBlank()) {
                canvas.drawText("Email: ${result.userProfile.email}", 52f, textY, paint)
                textY += 16f
            }
        } else {
            canvas.drawText("State: ${result.state}", 52f, textY, paint)
            textY += 18f
            canvas.drawText("Utility: ${result.tariffInfo.utilityProvider.substringBefore(" (")}", 52f, textY, paint)
            textY += 18f
            canvas.drawText("Tariff: ${result.connectionType.displayName}", 52f, textY, paint)
            textY += 18f
            canvas.drawText("Billing Cycle: ${result.billingPeriod.displayName.substringBefore(" (")}", 52f, textY, paint)
        }

        val col2X = 320f
        var col2Y = y + 44f
        canvas.drawText("Previous: ${result.previousReading.toInt()} kWh", col2X, col2Y, paint)
        col2Y += 16f
        canvas.drawText("Current: ${result.currentReading.toInt()} kWh", col2X, col2Y, paint)
        col2Y += 18f

        paint.color = Color.parseColor("#059669")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Consumption: ${result.unitsConsumed.toInt()} Units (kWh)", col2X, col2Y, paint)
        col2Y += 16f

        if (result.isCustomRate) {
            paint.color = Color.parseColor("#0284C7")
            canvas.drawText("Rate Fixing: INR ${result.customRatePerKwh}/kWh", col2X, col2Y, paint)
        } else {
            paint.color = Color.parseColor("#475569")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Official TANGEDCO Slabs Applied", col2X, col2Y, paint)
        }

        y += cardHeight + 16f

        // 3. Prominent Estimated Total Banner
        val heroRect = RectF(36f, y, (PAGE_WIDTH - 36).toFloat(), y + 65f)
        val heroPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#ECFDF5")
        }
        val heroBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.parseColor("#10B981")
            strokeWidth = 1.5f
        }
        canvas.drawRoundRect(heroRect, 10f, 10f, heroPaint)
        canvas.drawRoundRect(heroRect, 10f, 10f, heroBorder)

        paint.color = Color.parseColor("#065F46")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 14f
        canvas.drawText("ESTIMATED ELECTRICITY BILL AMOUNT:", 52f, y + 28f, paint)

        paint.color = Color.parseColor("#0B192C")
        paint.textSize = 26f
        canvas.drawText("INR ${String.format(Locale.US, "%.2f", result.estimatedTotal)}", 52f, y + 55f, paint)

        y += 85f

        // 4. Itemized Charges Table
        paint.color = Color.parseColor("#0B192C")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 13f
        canvas.drawText("ITEMIZED CHARGES BREAKDOWN", 36f, y + 15f, paint)

        y += 26f

        // Table Header
        val tableLeft = 36f
        val tableRight = (PAGE_WIDTH - 36).toFloat()
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRect(tableLeft, y, tableRight, y + 24f, paint)

        paint.color = Color.parseColor("#0F172A")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10.5f
        canvas.drawText("CHARGE DESCRIPTION", tableLeft + 12f, y + 16f, paint)
        canvas.drawText("AMOUNT (INR)", tableRight - 100f, y + 16f, paint)

        y += 24f

        fun drawTableRow(label: String, amount: String, isGreen: Boolean = false, isBold: Boolean = false) {
            paint.typeface = if (isBold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 10f
            paint.color = if (isGreen) Color.parseColor("#059669") else Color.parseColor("#1E293B")
            canvas.drawText(label, tableLeft + 12f, y + 16f, paint)
            canvas.drawText(amount, tableRight - 100f, y + 16f, paint)

            canvas.drawLine(tableLeft, y + 22f, tableRight, y + 22f, strokePaint)
            y += 22f
        }

        drawTableRow("Energy Charges", "INR ${String.format(Locale.US, "%.2f", result.energyCharges)}")
        drawTableRow("Fixed / Demand Charges", "INR ${String.format(Locale.US, "%.2f", result.fixedCharges)}")

        if (result.adjustments > 0.0) {
            drawTableRow("Electricity Duty / Adjustments", "INR ${String.format(Locale.US, "%.2f", result.adjustments)}")
        }

        if (result.subsidyAmount > 0.0) {
            drawTableRow("Government Subsidy (100 Free Units)", "-INR ${String.format(Locale.US, "%.2f", result.subsidyAmount)}", isGreen = true)
        }

        // Total row
        drawTableRow("Total Estimated Bill Payable", "INR ${String.format(Locale.US, "%.2f", result.estimatedTotal)}", isBold = true)

        y += 20f

        // 5. Slab breakdown table
        if (result.slabBreakdown.isNotEmpty()) {
            paint.color = Color.parseColor("#0B192C")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 12f
            canvas.drawText("SLAB-WISE ENERGY CHARGE COMPUTATION", 36f, y + 15f, paint)

            y += 24f

            paint.color = Color.parseColor("#F1F5F9")
            canvas.drawRect(tableLeft, y, tableRight, y + 22f, paint)

            paint.color = Color.parseColor("#0F172A")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 10f
            canvas.drawText("SLAB RANGE", tableLeft + 12f, y + 15f, paint)
            canvas.drawText("UNITS", tableLeft + 200f, y + 15f, paint)
            canvas.drawText("RATE / UNIT", tableLeft + 290f, y + 15f, paint)
            canvas.drawText("AMOUNT", tableRight - 100f, y + 15f, paint)

            y += 22f

            result.slabBreakdown.forEach { slab ->
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 9.5f
                paint.color = if (slab.isFreeSubsidy) Color.parseColor("#059669") else Color.parseColor("#334155")

                canvas.drawText(slab.rangeLabel, tableLeft + 12f, y + 14f, paint)
                canvas.drawText("${slab.unitsInSlab.toInt()} kWh", tableLeft + 200f, y + 14f, paint)
                canvas.drawText("INR ${slab.ratePerUnit}", tableLeft + 290f, y + 14f, paint)
                canvas.drawText(if (slab.isFreeSubsidy) "FREE" else "INR ${String.format(Locale.US, "%.2f", slab.amount)}", tableRight - 100f, y + 14f, paint)

                canvas.drawLine(tableLeft, y + 18f, tableRight, y + 18f, strokePaint)
                y += 18f
            }
        }

        y += 20f

        // 6. Verification and Disclaimer Box
        val noticeRect = RectF(36f, y, (PAGE_WIDTH - 36).toFloat(), y + 70f)
        val noticePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F8FAFC")
        }
        canvas.drawRoundRect(noticeRect, 8f, 8f, noticePaint)
        canvas.drawRoundRect(noticeRect, 8f, 8f, strokePaint)

        paint.color = Color.parseColor("#0F172A")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f
        canvas.drawText("Regulatory Verification:", 48f, y + 18f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.color = Color.parseColor("#64748B")
        canvas.drawText("Calculated using official ${result.tariffInfo.regulatoryAuthority} tariff schedule.", 48f, y + 32f, paint)
        canvas.drawText("Effective from: ${result.tariffInfo.effectiveDate} • Utility: ${result.tariffInfo.utilityProvider}", 48f, y + 44f, paint)
        canvas.drawText("Disclaimer: This amount is an estimate based on readings entered. Actual electricity bills may vary slightly due to meter rent or fuel adjustments.", 48f, y + 58f, paint)

        // Footer
        paint.textSize = 8f
        paint.color = Color.parseColor("#94A3B8")
        canvas.drawText("EBCT (Elect Fast) — Know Your Electricity Bill Before It Arrives", 36f, PAGE_HEIGHT - 20f, paint)
        canvas.drawText("Page 1 of 1", (PAGE_WIDTH - 80).toFloat(), PAGE_HEIGHT - 20f, paint)

        pdfDocument.finishPage(page)

        // Write to file
        val outputDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val fileName = "EBCT_ElectFast_Bill_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.pdf"
        val outputFile = File(outputDir, fileName)

        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }

    fun sharePdf(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "EBCT (Elect Fast) Electricity Bill Estimate")
            putExtra(Intent.EXTRA_TEXT, "Here is your electricity bill estimate statement generated by EBCT (Electricity Bill Calculate Fast).")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Share or Save EBCT Bill PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun generateAndShareBillPdf(context: Context, result: BillCalculationResult) {
        try {
            val file = generateBillPdf(context, result)
            sharePdf(context, file)
            Toast.makeText(context, "PDF generated successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to generate PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun generateAndShareRecordPdf(context: Context, record: BillRecord) {
        val connType = when {
            record.connectionType.contains("Commercial", ignoreCase = true) -> com.example.data.model.ConnectionType.COMMERCIAL
            record.connectionType.contains("Industrial", ignoreCase = true) -> com.example.data.model.ConnectionType.INDUSTRIAL
            else -> com.example.data.model.ConnectionType.DOMESTIC
        }
        val billingPeriod = if (record.billingPeriod.contains("Monthly", ignoreCase = true) && !record.billingPeriod.contains("Bi-Monthly", ignoreCase = true)) {
            com.example.data.model.BillingPeriod.MONTHLY
        } else {
            com.example.data.model.BillingPeriod.BI_MONTHLY
        }
        val result = com.example.data.repository.TariffRepository().calculateBill(
            state = record.state,
            connectionType = connType,
            billingPeriod = billingPeriod,
            previousReading = record.previousReading,
            currentReading = record.currentReading
        )
        generateAndShareBillPdf(context, result)
    }
}
