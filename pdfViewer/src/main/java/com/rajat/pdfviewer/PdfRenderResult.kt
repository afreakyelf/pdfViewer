package com.rajat.pdfviewer

import android.graphics.Bitmap

sealed class PdfRenderResult(open val pageNo: Int) {
    data class Error(override val pageNo: Int, val cause: Throwable? = null) : PdfRenderResult(pageNo)
    data class Success(override val pageNo: Int, val bitmap: Bitmap) : PdfRenderResult(pageNo)
}
