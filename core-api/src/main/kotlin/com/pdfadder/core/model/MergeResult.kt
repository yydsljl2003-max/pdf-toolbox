package com.pdfadder.core.model

import android.net.Uri

/**
 * 合并操作的结果
 */
data class MergeResult(
    val success: Boolean,
    val outputFileUri: Uri? = null,
    val errorMessage: String? = null,
    val fileName: String? = null
)

/**
 * PDF 文件的基础信息（页数、大小）
 */
data class PdfInfo(
    val pageCount: Int,
    val fileSize: Long
)