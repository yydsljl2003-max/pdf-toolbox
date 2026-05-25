package com.pdfadder.core.model

import android.net.Uri

/**
 * 用户选择的单个 PDF 文件信息
 */
data class PdfFile(
    val uri: Uri,
    val displayName: String,
    val fileSize: Long,
    val pageCount: Int,
    var sortOrder: Int
)