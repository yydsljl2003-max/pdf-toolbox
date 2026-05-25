package com.pdfadder.app

import android.app.Application
import com.pdfadder.app.di.FeatureRegistry
import com.pdfadder.feature.merge.MergeSpiImpl

class PdfAdderApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // 注册所有 PDF 功能模块
        FeatureRegistry.registerAll(listOf(
            MergeSpiImpl(this)
            // 后续添加新模块：
            // WordConvertSpiImpl(this),
            // PageRotateSpiImpl(this),
            // PageDeleteSpiImpl(this)
        ))
    }
}
