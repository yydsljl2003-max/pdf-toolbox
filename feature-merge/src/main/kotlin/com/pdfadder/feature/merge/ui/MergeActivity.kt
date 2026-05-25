package com.pdfadder.feature.merge.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pdfadder.core.model.PdfFile
import com.pdfadder.core.model.MergeResult
import com.pdfadder.feature.merge.R
import com.pdfadder.feature.merge.databinding.ActivityMergeBinding
import com.pdfadder.feature.merge.vm.MergeViewModel

class MergeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMergeBinding
    private val viewModel: MergeViewModel by viewModels()
    private lateinit var adapter: MergeAdapter

    companion object {
        const val TAG = "MergeActivity"
        const val EXTRA_URIS = "extra_uris"
    }

    private val openFilesLauncher = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        Log.d(TAG, "onActivityResult: uris.size=" + (uris?.size ?: 0))
        if (!uris.isNullOrEmpty()) {
            viewModel.addFiles(uris, this)
        }
    }

    // ?????????SAF?
    private val saveFileLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        Log.d(TAG, "saveFileLauncher: uri=" + uri.toString())
        if (uri != null) {
            viewModel.merge(uri, this)
        } else {
            Log.w(TAG, "saveFileLauncher: user cancelled")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMergeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        setupObservers()
        handleIntentPdfUris(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: intent=" + intent.toString())
        setIntent(intent)
        handleIntentPdfUris(intent)
    }

    private fun handleIntentPdfUris(intent: Intent?) {
        if (intent == null) {
            Log.w(TAG, "handleIntentPdfUris: intent is null")
            return
        }
        when (intent.action) {
            Intent.ACTION_VIEW -> {
                Log.d(TAG, "handleIntentPdfUris: ACTION_VIEW")
                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.data
                } else {
                    @Suppress("DEPRECATION")
                    intent.data
                }
                uri?.let {
                    viewModel.addFiles(listOf(it), this)
                }
            }
            Intent.ACTION_SEND -> {
                Log.d(TAG, "handleIntentPdfUris: ACTION_SEND")
                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_STREAM)
                }
                uri?.let {
                    viewModel.addFiles(listOf(it), this)
                }
            }
            else -> {
                intent.getStringArrayListExtra(EXTRA_URIS)?.let { uriStrings ->
                    if (uriStrings.isNotEmpty()) {
                        val uris = uriStrings.map { Uri.parse(it) }
                        viewModel.addFiles(uris, this)
                    }
                }
            }
        }
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = MergeAdapter(
            onItemMove = { from, to ->
                viewModel.repositionFiles(from, to)
            },
            onItemClick = {}
        )
        binding.recyclerViewFiles.apply {
            layoutManager = LinearLayoutManager(this@MergeActivity)
            adapter = this@MergeActivity.adapter
        }

        val itemTouchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

            override fun onMove(
                recyclerView: RecyclerView,
                source: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPos = source.bindingAdapterPosition
                val toPos = target.bindingAdapterPosition
                viewModel.repositionFiles(fromPos, toPos)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

            override fun onSelectedChanged(
                viewHolder: RecyclerView.ViewHolder?, actionState: Int
            ) {
                if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                    viewHolder?.itemView?.alpha = 0.6f
                }
                super.onSelectedChanged(viewHolder, actionState)
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                viewHolder.itemView.alpha = 1.0f
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewFiles)
    }

    private fun setupObservers() {
        viewModel.pdfFiles.observe(this) { files ->
            adapter.submitList(files?.toMutableList())
            updateFileCountText(files ?: emptyList())
            binding.btnMerge.isEnabled = !files.isNullOrEmpty()
        }

        viewModel.mergeResult.observe(this) { result ->
            onMergeResult(result)
        }
    }

    private fun setupClickListeners() {
        binding.btnSelectFiles.setOnClickListener {
            openFilesLauncher.launch(arrayOf("application/pdf"))
        }

        binding.btnMerge.setOnClickListener {
            // ?? SAF ?????????
            saveFileLauncher.launch("merged.pdf")
        }
    }

    private fun updateFileCountText(files: List<PdfFile>) {
        val count = files.size
        val pages = files.sumOf { it.pageCount }
        binding.tvFileCount.text = if (count == 0) {
            getString(R.string.no_files_selected)
        } else {
            getString(R.string.file_count, count, pages)
        }
    }

    private fun onMergeResult(result: MergeResult) {
        if (result.success) {
            val fileName = result.fileName ?: "merged.pdf"
            val message = "File \"" + fileName + "\" saved"
            AlertDialog.Builder(this)
                .setTitle("Merge Complete")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .setNeutralButton("Share") { _, _ -> sharePdf(result) }
                .show()
        } else {
            Toast.makeText(
                this,
                getString(R.string.merge_failed, result.errorMessage ?: "Unknown error"),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun sharePdf(result: MergeResult) {
        val fileUri = result.outputFileUri ?: run {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            startActivity(Intent.createChooser(shareIntent, "Share PDF"))
        } catch (e: Exception) {
            Log.e(TAG, "sharePdf failed", e)
            Toast.makeText(this, "Share failed: " + e.message, Toast.LENGTH_SHORT).show()
        }
    }
}
