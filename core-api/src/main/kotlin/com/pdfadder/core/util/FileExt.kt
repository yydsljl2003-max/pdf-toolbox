package com.pdfadder.core.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

/**
 * 从 Uri 中提取显示文件名
 */
fun Uri.getFileName(context: Context): String {
    var name = "unknown.pdf"
    context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && cursor.moveToFirst()) {
            name = cursor.getString(nameIndex)
        }
    }
    return name
}

/**
 * 从 Uri 中获取文件大小
 */
fun Uri.getFileSize(context: Context): Long {
    var size = 0L
    context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (sizeIndex >= 0 && cursor.moveToFirst()) {
            size = cursor.getLong(sizeIndex)
        }
    }
    return size
}

/**
 * 格式化文件大小为可读字符串
 */
fun Long.formatFileSize(): String {
    return when {
        this < 1024 -> " B"
        this < 1024 * 1024 -> " KB"
        this < 1024L * 1024 * 1024 -> " MB"
        else -> " GB"
    }
}
