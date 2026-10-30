package com.example.export

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.KickSessionEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    fun generateDoctorPdf(
        context: Context,
        sessions: List<KickSessionEntity>,
        patientName: String = "Expectant Mother",
        gestationalAgeWeeks: String = "28+ Weeks"
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 page
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(214, 90, 104) // Rose primary
            textSize = 20f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.rgb(33, 26, 27)
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.rgb(70, 70, 75)
            textSize = 10f
            isAntiAlias = true
        }

        val accentBoxPaint = Paint().apply {
            color = Color.rgb(255, 240, 242)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.rgb(220, 220, 225)
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        val chartBarPaint = Paint().apply {
            color = Color.rgb(76, 106, 100) // Sage secondary
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // 1. Header Banner
        canvas.drawText("FETAL MOVEMENT & KICK COUNT REPORT", 40f, 50f, titlePaint)
        canvas.drawText("Prenatal Health Monitoring Log for OB/GYN or Midwife Review", 40f, 68f, bodyPaint)

        // Metadata box
        val metaRect = RectF(40f, 85f, 555f, 135f)
        canvas.drawRoundRect(metaRect, 8f, 8f, accentBoxPaint)

        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        val generatedDate = dateFormat.format(Date())

        canvas.drawText("Patient: $patientName", 55f, 105f, headerPaint)
        canvas.drawText("Gestational Age: $gestationalAgeWeeks", 55f, 122f, bodyPaint)
        canvas.drawText("Report Generated: $generatedDate", 320f, 105f, bodyPaint)
        canvas.drawText("Total Sessions: ${sessions.size}", 320f, 122f, bodyPaint)

        // 2. Summary Statistics
        val totalSessions = sessions.size
        val completedTarget = sessions.count { it.isTargetReached }
        val avgDurationSec = if (sessions.isNotEmpty()) {
            sessions.map { it.durationSeconds }.average().toLong()
        } else 0L
        val avgDurationMins = avgDurationSec / 60

        var currentY = 160f
        canvas.drawText("CLINICAL SUMMARY & ADHERENCE", 40f, currentY, headerPaint)
        currentY += 15f

        val statRect = RectF(40f, currentY, 555f, currentY + 45f)
        val statBoxPaint = Paint().apply {
            color = Color.rgb(245, 245, 248)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(statRect, 6f, 6f, statBoxPaint)

        canvas.drawText("Sessions Meeting Target: $completedTarget / $totalSessions (${if (totalSessions > 0) (completedTarget * 100 / totalSessions) else 0}%)", 55f, currentY + 22f, bodyPaint)
        canvas.drawText("Avg Time to Complete: $avgDurationMins minutes", 55f, currentY + 36f, bodyPaint)

        val vigCount = sessions.count { it.movementStrength == 3 }
        val modCount = sessions.count { it.movementStrength == 2 }
        val gentleCount = sessions.count { it.movementStrength == 1 }
        val totalWaterGlasses = sessions.sumOf { it.waterIntakeGlasses }
        canvas.drawText("Movement Strength: $vigCount Vig, $modCount Mod, $gentleCount Gentle", 320f, currentY + 22f, bodyPaint)
        canvas.drawText("Hydration Logged: $totalWaterGlasses glasses (${totalWaterGlasses * 250} ml)", 320f, currentY + 36f, bodyPaint)
        currentY += 60f

        // 3. Mini Trend Chart (Duration across recent sessions)
        if (sessions.isNotEmpty()) {
            canvas.drawText("SESSION DURATION TREND (Minutes to Target)", 40f, currentY, headerPaint)
            currentY += 15f

            val chartBox = RectF(40f, currentY, 555f, currentY + 70f)
            canvas.drawRoundRect(chartBox, 4f, 4f, statBoxPaint)

            val recentSessions = sessions.take(12).reversed()
            val maxDurMins = recentSessions.maxOfOrNull { (it.durationSeconds / 60).toFloat() }?.coerceAtLeast(30f) ?: 120f
            val barWidth = (500f / recentSessions.size.coerceAtLeast(1)).coerceAtMost(32f)

            recentSessions.forEachIndexed { index, sess ->
                val durMins = (sess.durationSeconds / 60).toFloat()
                val barHeight = (durMins / maxDurMins) * 45f
                val left = 55f + index * (barWidth + 8f)
                val top = (currentY + 60f) - barHeight
                val right = left + barWidth
                val bottom = currentY + 60f

                val barColor = if (sess.isTargetReached) chartBarPaint else Paint().apply {
                    color = Color.rgb(229, 169, 59) // Amber for incomplete
                    style = Paint.Style.FILL
                }
                canvas.drawRect(left, top, right, bottom, barColor)
                canvas.drawText("${durMins.toInt()}m", left + 2f, top - 3f, Paint().apply {
                    textSize = 7f
                    color = Color.DKGRAY
                })
            }
            currentY += 85f
        }

        // 4. Detailed Sessions Table
        canvas.drawText("RECORDED SESSIONS LOG", 40f, currentY, headerPaint)
        currentY += 15f

        // Table Header
        canvas.drawLine(40f, currentY, 555f, currentY, linePaint)
        currentY += 12f
        canvas.drawText("Date & Time", 45f, currentY, headerPaint)
        canvas.drawText("Duration", 175f, currentY, headerPaint)
        canvas.drawText("Kicks / Target", 255f, currentY, headerPaint)
        canvas.drawText("Strength", 345f, currentY, headerPaint)
        canvas.drawText("Water", 415f, currentY, headerPaint)
        canvas.drawText("Notes", 465f, currentY, headerPaint)
        currentY += 6f
        canvas.drawLine(40f, currentY, 555f, currentY, linePaint)
        currentY += 14f

        val sessionDateFmt = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
        val displaySessions = sessions.take(15) // Max fit on page 1

        for (sess in displaySessions) {
            val dateStr = sessionDateFmt.format(Date(sess.startTimeMs))
            val mins = sess.durationSeconds / 60
            val secs = sess.durationSeconds % 60
            val durStr = "${mins}m ${secs}s"
            val targetStr = if (sess.targetKicks > 0) "${sess.totalKicks} / ${sess.targetKicks}" else "${sess.totalKicks} (free)"
            val strengthStr = when (sess.movementStrength) {
                3 -> "Vigorous"
                2 -> "Moderate"
                else -> "Gentle"
            }
            val waterStr = if (sess.waterIntakeGlasses > 0) "${sess.waterIntakeGlasses * 250}ml" else "-"
            val noteSnippet = if (sess.notes.isNotBlank()) sess.notes.take(14) else "-"

            canvas.drawText(dateStr, 45f, currentY, bodyPaint)
            canvas.drawText(durStr, 175f, currentY, bodyPaint)
            canvas.drawText(targetStr, 255f, currentY, bodyPaint)
            canvas.drawText(strengthStr, 345f, currentY, bodyPaint)
            canvas.drawText(waterStr, 415f, currentY, bodyPaint)
            canvas.drawText(noteSnippet, 465f, currentY, bodyPaint)

            currentY += 16f
            if (currentY > 740f) break
        }

        // Doctor Review Sign-off Section at bottom
        val signY = 780f
        canvas.drawLine(40f, signY, 555f, signY, linePaint)
        canvas.drawText("Provider Notes / Follow-up: ____________________________________________________", 40f, signY + 20f, bodyPaint)
        canvas.drawText("Healthcare Provider Signature: ___________________________   Date: _____________", 40f, signY + 38f, bodyPaint)

        pdfDocument.finishPage(page)

        // Save PDF to cache reports directory
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }
        val file = File(reportsDir, "fetal_kick_report_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return file
    }

    fun sharePdf(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Fetal Kick Count Activity Report")
            putExtra(Intent.EXTRA_TEXT, "Attached is my prenatal fetal movement and kick count report for clinical review.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Share Doctor PDF Report")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
