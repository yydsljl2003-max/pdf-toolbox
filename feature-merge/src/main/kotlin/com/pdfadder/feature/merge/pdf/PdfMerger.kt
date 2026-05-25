package com.pdfadder.feature.merge.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.pdfadder.core.model.PdfFile
import com.pdfadder.core.model.MergeResult
import java.io.IOException

class PdfMerger {
    companion object {
        private const val TAG = "PdfMerger"
    }

    fun merge(
        pdfFiles: List<PdfFile>,
        outputUri: Uri,
        context: Context
    ): MergeResult {
        try {
            if (pdfFiles.isEmpty()) {
                return MergeResult(success = false, errorMessage = "No files to merge")
            }

            Log.d(TAG, "=== PdfMerger START: " + pdfFiles.size + " files ===")

            // Step 1: Count total pages across all files
            var totalPages = 0
            pdfFiles.forEach { pdfFile ->
                val pageCount = getPageCount(pdfFile.uri, context)
                totalPages += pageCount
            }
            Log.d(TAG, "    total pages: " + totalPages)

            if (totalPages == 0) {
                return MergeResult(success = false, errorMessage = "All PDF files have no valid pages")
            }

            // Get device DPI for resolution-aware rendering
            val deviceDpi = context.resources.displayMetrics.densityDpi

            // Step 2: Create merged PDF document
            val mergedDoc = PdfDocument()

            // Step 3: Render each source page into merged document
            var currentPageIndex = 0
            pdfFiles.forEachIndexed { fileIndex, pdfFile ->
                Log.d(TAG, "    merging file[" + fileIndex + "]: " + pdfFile.displayName)

                val fileDescriptor = context.contentResolver.openFileDescriptor(pdfFile.uri, "r")
                    ?: throw IOException("Cannot open file: " + pdfFile.displayName)

                val renderer = PdfRenderer(fileDescriptor)
                for (pageIndex in 0 until renderer.pageCount) {
                    val sourcePage = renderer.openPage(pageIndex)

                    // Calculate render resolution from native page size and device DPI
                    val renderWidth = (sourcePage.width * deviceDpi) / 72
                    val renderHeight = (sourcePage.height * deviceDpi) / 72

                    // Render source page to bitmap at native resolution
                    val bitmap = Bitmap.createBitmap(renderWidth, renderHeight, Bitmap.Config.ARGB_8888)
                    sourcePage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                    // Create a new page matching the source page dimensions
                    val pageInfo = PdfDocument.PageInfo.Builder(renderWidth, renderHeight, currentPageIndex + 1).create()
                    val page = mergedDoc.startPage(pageInfo)
                    val canvas = page.canvas
                    canvas.drawColor(android.graphics.Color.WHITE)
                    canvas.drawBitmap(bitmap, 0f, 0f, null)
                    mergedDoc.finishPage(page)

                    sourcePage.close()
                    currentPageIndex++
                }

                renderer.close()
                fileDescriptor.close()
            }

            // Step 4: Write merged document to outputUri
            context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                mergedDoc.writeTo(outputStream)
            }
            mergedDoc.close()

            // Get the actual file name from URI
            val fileName = getFileNameFromUri(outputUri, context)

            Log.d(TAG, "=== merge SUCCESS: uri=" + outputUri.toString() + " ===")
            return MergeResult(
                success = true,
                outputFileUri = outputUri,
                fileName = fileName,
                errorMessage = null
            )
        } catch (e: Exception) {
            Log.e(TAG, "=== merge FAILED ===", e)
            return MergeResult(success = false, errorMessage = e.message ?: "Merge failed")
        }
    }

    private fun getPageCount(uri: Uri, context: Context): Int {
        return try {
            val fd = context.contentResolver.openFileDescriptor(uri, "r")
                ?: return 0
            val renderer = PdfRenderer(fd)
            val count = renderer.pageCount
            renderer.close()
            fd.close()
            count
        } catch (e: Exception) {
            Log.w(TAG, "getPageCount failed: " + e.message)
            0
        }
    }

    private fun getFileNameFromUri(uri: Uri, context: Context): String {
        var name = "merged.pdf"
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && cursor.moveToFirst()) {
                    name = cursor.getString(idx)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "getFileNameFromUri: " + e.message)
        }
        return name
    }
}
