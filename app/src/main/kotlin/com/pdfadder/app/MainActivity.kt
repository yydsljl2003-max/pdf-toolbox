package com.pdfadder.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pdfadder.app.databinding.ActivityMainBinding
import com.pdfadder.app.databinding.ItemFeatureBinding
import com.pdfadder.app.di.FeatureRegistry

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        renderFeatureButtons()
    }

    private fun renderFeatureButtons() {
        val features = FeatureRegistry.features

        features.forEach { feature ->
            val itemBinding = ItemFeatureBinding.inflate(layoutInflater)
            val itemView = itemBinding.root

            // 设置名称和描述
            itemBinding.tvFeatureName.text = feature.featureName
            itemBinding.tvFeatureDescription.text = feature.featureDescription

            // 设置图标（如果有）
            feature.featureIconResId?.let {
                itemBinding.ivFeatureIcon.setImageResource(it)
            }

            // 点击跳转功能页面
            itemBinding.root.setOnClickListener {
                feature.launch(this)
            }

            binding.featuresContainer.addView(itemView)
        }
    }
}
