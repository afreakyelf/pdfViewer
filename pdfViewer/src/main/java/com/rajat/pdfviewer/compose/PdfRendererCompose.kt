package com.rajat.pdfviewer.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.rajat.pdfviewer.HeaderData
import com.rajat.pdfviewer.PdfRendererView
import com.rajat.pdfviewer.PdfSource
import com.rajat.pdfviewer.PinchZoomRecyclerView
import com.rajat.pdfviewer.RenderQuality

@Composable
fun Pdf(
    modifier: Modifier = Modifier,
    source: PdfSource,
    renderQuality: RenderQuality = RenderQuality.LOW,
    headers: HeaderData = HeaderData(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    statusCallBack: PdfRendererView.StatusCallBack? = null,
    scrolledToTop: (Boolean) -> Unit = {},
    onPageRendered: (page: Int) -> Unit = {},
) {
    val lifecycleScope = lifecycleOwner.lifecycleScope

    AndroidView(
        factory = { context ->
            PdfRendererView(context).apply {
                if (statusCallBack != null) statusListener = statusCallBack
                this.renderQuality = renderQuality
                init(source, headers, lifecycleScope, lifecycleOwner.lifecycle)
                (recyclerView as? PinchZoomRecyclerView)?.onTopChange = {
                    scrolledToTop(it)
                }
                pageRenderListener = onPageRendered
            }
        },
        update = { pdfView ->
            if (pdfView.pdfSource != source) {
                pdfView.init(source, headers, lifecycleScope, lifecycleOwner.lifecycle)
            }
        },
        modifier = modifier
    )
}
