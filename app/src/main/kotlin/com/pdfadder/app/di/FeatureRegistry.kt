package com.pdfadder.app.di

import com.pdfadder.core.spi.PdfFeatureSpi

/**
 * 功能注册中心
 * 所有 PDF 功能模块在此注册，主界面读取并渲染入口按钮
 * 新增模块只需调用 register()，不修改已有代码
 */
object FeatureRegistry {
    private val _features = mutableListOf<PdfFeatureSpi>()

    val features: List<PdfFeatureSpi> get() = _features

    fun register(feature: PdfFeatureSpi) {
        _features.add(feature)
    }

    fun registerAll(newFeatures: List<PdfFeatureSpi>) {
        _features.addAll(newFeatures)
    }

    fun clear() {
        _features.clear()
    }
}
