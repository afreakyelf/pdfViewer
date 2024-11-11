package com.rajat.pdfviewer

import android.net.Uri
import java.io.File

sealed class PdfSource {
    data class FromUrl(val url: String): PdfSource()
    data class FromFile(val file: File): PdfSource()
    data class FromUri(val uri: Uri): PdfSource()
}