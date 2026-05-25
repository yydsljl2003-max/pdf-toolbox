package com.pdfadder.feature.merge.vm

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdfadder.core.model.PdfFile
import com.pdfadder.core.model.MergeResult
import com.pdfadder.core.model.PdfInfo
import com.pdfadder.core.util.getFileName
import com.pdfadder.core.util.getFileSize
import com.pdfadder.feature.merge.pdf.PdfMerger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MergeViewModel : ViewModel() {

    companion object {
        const val TAG = "MergeViewModel"
    }

    private val _pdfFiles = MutableLiveData<MutableList<PdfFile>>(mutableListOf())
    val pdfFiles: LiveData<MutableList<PdfFile>> = _pdfFiles

    private val _mergeResult = MutableLiveData<MergeResult>()
    val mergeResult: LiveData<MergeResult> = _mergeResult

    private val _mergeProgress = MutableLiveData<Boolean>()
    val mergeProgress: LiveData<Boolean> = _mergeProgress

    fun addFiles(uris: List<Uri>, context: Context) {
        Log.d(TAG, "addFiles: uris.size=" + uris.size)
        try {
            val newFiles = uris.mapIndexed { index, uri ->
                val fileName = uri.getFileName(context)
                val fileSize = uri.getFileSize(context)
                PdfFile(
                    uri = uri,
                    displayName = fileName,
                    fileSize = fileSize,
                    pageCount = 0,
                    sortOrder = _pdfFiles.value!!.size + index
                )
            }
            val newList = (_pdfFiles.value ?: mutableListOf()).toMutableList()
            newList.addAll(newFiles)
            _pdfFiles.value = newList
            Log.d(TAG, "addFiles: total=" + newList.size)
        } catch (e: Exception) {
            Log.e(TAG, "addFiles: exception=" + e.message, e)
        }
    }

    fun removeFile(position: Int) {
        val newList = (_pdfFiles.value ?: mutableListOf()).toMutableList()
        newList.removeAt(position)
        _pdfFiles.value = newList
    }

    fun repositionFiles(fromPosition: Int, toPosition: Int) {
        val list = (_pdfFiles.value ?: mutableListOf()).toMutableList()
        if (fromPosition < 0 || toPosition < 0 || fromPosition >= list.size || toPosition >= list.size) return
        val item = list.removeAt(fromPosition)
        list.add(toPosition, item)
        list.forEachIndexed { index, file ->
            file.sortOrder = index
        }
        _pdfFiles.value = list
    }

    fun merge(outputUri: Uri, context: Context) {
        val files = _pdfFiles.value ?: return
        if (files.isEmpty()) return

        _mergeProgress.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val result = PdfMerger().merge(files, outputUri, context)
            _mergeResult.postValue(result)
            _mergeProgress.postValue(false)
        }
    }

    val hasFiles: Boolean
        get() = _pdfFiles.value?.isNotEmpty() == true

    val fileCount: Int
        get() = _pdfFiles.value?.size ?: 0

    val totalPageCount: Int
        get() = _pdfFiles.value?.sumOf { it.pageCount } ?: 0
}
