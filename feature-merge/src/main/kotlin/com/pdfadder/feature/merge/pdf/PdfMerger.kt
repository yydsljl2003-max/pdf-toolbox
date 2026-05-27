package com.pdfadder.feature.merge.pdf

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.artifex.mupdf.fitz.Document
import com.artifex.mupdf.fitz.PDFDocument
import com.pdfadder.core.model.PdfFile
import com.pdfadder.core.model.MergeResult
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class PdfMerger {
    companion object {
        private const val TAG = "PdfMerger"
    }

    fun merge(
        pdfFiles: List<PdfFile>,
        outputUri: Uri,
        context: Context
    ): MergeResult {
        if (pdfFiles.isEmpty()) {
            return MergeResult(success = false, errorMessage = "No files to merge")
        }

        Log.d(TAG, "=== PdfMerger START: ${pdfFiles.size} files ===")

        // Initialize MuPDF native context (idempotent)
        com.artifex.mupdf.fitz.Context.init()

        val tempFiles = mutableListOf<File>()
        var newDoc: PDFDocument? = null

        try {
            // Create new empty PDF document using in-memory store
            newDoc = PDFDocument()

            var totalPageCount = 0
            pdfFiles.forEachIndexed { fileIndex, pdfFile ->
                Log.d(TAG, "    merging file[$fileIndex]: ${pdfFile.displayName}")

                val tempFile = copyToTempFile(pdfFile.uri, context)
                tempFiles.add(tempFile)

                var srcDoc: PDFDocument? = null
                try {
                    srcDoc = Document.openDocument(tempFile.absolutePath).asPDF()
                    val pageCount = srcDoc.countPages()
                    Log.d(TAG, "    source pages: $pageCount")

                    for (pageIndex in 0 until pageCount) {
                        newDoc!!.graftPage(totalPageCount, srcDoc, pageIndex)
                        totalPageCount++
                    }
                } finally {
                    srcDoc?.destroy()
                }
            }

            Log.d(TAG, "    total pages merged: $totalPageCount")

            // Save to a temp file (MuPDF save() requires a file path)
            val saveTempFile = File(context.cacheDir, "merged_${UUID.randomUUID()}.pdf")
            newDoc!!.save(saveTempFile.absolutePath, "pdf")

            // Copy from temp file to output URI
            context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                saveTempFile.inputStream().copyTo(outputStream)
            }

            val fileName = getFileNameFromUri(outputUri, context)
            Log.d(TAG, "=== merge SUCCESS: uri=$outputUri ===")
            return MergeResult(
                success = true,
                outputFileUri = outputUri,
                fileName = fileName,
                errorMessage = null
            )
        } catch (e: Exception) {
            Log.e(TAG, "=== merge FAILED ===", e)
            return MergeResult(success = false, errorMessage = e.message ?: "Merge failed")
        } finally {
            newDoc?.destroy()
            tempFiles.forEach { file ->
                try {
                    file.delete()
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to delete temp: ${file.absolutePath}")
                }
            }
            // Clean up save temp files
            try {
                context.cacheDir.listFiles { _, name -> name.startsWith("merged_") && name.endsWith(".pdf") }
                    ?.forEach { it.delete() }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to clean up save temp: ${e.message}")
            }
        }
    }

    private fun copyToTempFile(uri: Uri, context: Context): File {
        val tempFile = File(context.cacheDir, "mupdf_${UUID.randomUUID()}.pdf")
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                outputStream.flush()
            }
        }
        return tempFile
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
            Log.w(TAG, "getFileNameFromUri: ${e.message}")
        }
        return name
    }
}
