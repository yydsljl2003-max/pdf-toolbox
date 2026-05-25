package com.pdfadder.feature.merge.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pdfadder.core.model.PdfFile
import com.pdfadder.core.util.formatFileSize
import com.pdfadder.feature.merge.R
import com.pdfadder.feature.merge.databinding.ItemFileBinding

class MergeAdapter(
    private val onItemMove: (fromPosition: Int, toPosition: Int) -> Unit,
    private val onItemClick: (position: Int) -> Unit
) : ListAdapter<PdfFile, MergeAdapter.FileViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class FileViewHolder(
        private val binding: ItemFileBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pdfFile: PdfFile, position: Int) {
            binding.tvFileName.text = pdfFile.displayName
            binding.tvPageCount.text = "页"
            binding.root.setOnClickListener {
                onItemClick(position)
            }
            binding.root.setOnLongClickListener {
                // 长按触发拖拽，由 ItemTouchHelper 处理
                false
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PdfFile>() {
            override fun areItemsTheSame(oldItem: PdfFile, newItem: PdfFile): Boolean {
                return oldItem.uri == newItem.uri
            }

            override fun areContentsTheSame(oldItem: PdfFile, newItem: PdfFile): Boolean {
                return oldItem == newItem
            }
        }
    }
}
