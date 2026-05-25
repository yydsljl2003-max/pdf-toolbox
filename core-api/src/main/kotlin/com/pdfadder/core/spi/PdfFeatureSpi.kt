package com.pdfadder.core.spi

import android.content.Context
import android.content.Intent

/**
 * 每个 PDF 功能模块必须实现的接口。
 * 新增功能：新建 Gradle 模块 + 实现此接口 + 在 app 模块注册，无需改已有代码。
 */
interface PdfFeatureSpi {

    /** 功能显示名称，用于主界面入口按钮 */
    val featureName: String

    /** 功能描述文字，显示在按钮下方 */
    val featureDescription: String

    /** 功能图标资源 ID（可选，null 则用默认图标） */
    val featureIconResId: Int?

    /** 启动该功能的 Intent */
    fun createLaunchIntent(context: Context): Intent

    /**
     * 启动该功能（内部调用 createLaunchIntent）
     * @param context 应用上下文
     */
    fun launch(context: Context)
}
