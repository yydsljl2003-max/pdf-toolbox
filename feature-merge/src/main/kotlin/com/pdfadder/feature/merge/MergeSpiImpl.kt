package com.pdfadder.feature.merge

import android.content.Context
import android.content.Intent
import com.pdfadder.core.model.PdfFile
import com.pdfadder.core.model.MergeResult
import com.pdfadder.core.spi.PdfFeatureSpi
import com.pdfadder.feature.merge.R
import com.pdfadder.feature.merge.ui.MergeActivity

class MergeSpiImpl(
    private val context: Context
) : PdfFeatureSpi {

    override val featureName: String
        get() = context.getString(R.string.merge_feature_name)

    override val featureDescription: String
        get() = context.getString(R.string.merge_feature_desc)

    override val featureIconResId: Int?
        get() = null

    override fun createLaunchIntent(context: Context): Intent {
        return Intent(context, MergeActivity::class.java)
    }

    override fun launch(context: Context) {
        context.startActivity(createLaunchIntent(context))
    }
}
