package com.rajat.pdfviewer

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.pdf.PdfRenderer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.rajat.pdfviewer.databinding.ListItemPdfPageBinding
import com.rajat.pdfviewer.util.CommonUtils
import com.rajat.pdfviewer.util.hide
import com.rajat.pdfviewer.util.show
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.compareTo
import kotlin.div
import kotlin.text.toFloat
import kotlin.text.toInt
import kotlin.times

/**
 * Created by Rajat on 11,July,2020
 */

internal class PdfViewAdapter(
    private val context: Context,
    private val renderer: PdfRendererCore,
    private val pageSpacing: Rect,
    private val enableLoadingForPages: Boolean,
    private val renderQuality: RenderQuality,
) : RecyclerView.Adapter<PdfViewAdapter.PdfPageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfPageViewHolder =
        PdfPageViewHolder(ListItemPdfPageBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = renderer.getPageCount()

    override fun onBindViewHolder(holder: PdfPageViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class PdfPageViewHolder(private val itemBinding: ListItemPdfPageBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(position: Int) {
            with(itemBinding) {
                pageLoadingLayout.pdfViewPageLoadingProgress.visibility = if (enableLoadingForPages) View.VISIBLE else View.GONE

                renderer.getPageDimensionsAsync(position) { size ->
                    val width = (pageView.width.takeIf { it > 0 } ?: context.resources.displayMetrics.widthPixels)
                    val aspectRatio = size.width.toFloat() / size.height.toFloat()
                    val bitmapWidth = (width * (renderQuality.ordinal * aspectRatio + 1)).toInt()
                    val height = (width / aspectRatio).toInt()
                    val bitmapHeight = (height * (renderQuality.ordinal * aspectRatio + 1)).toInt()

                    updateLayoutParams(height)

                    val bitmap = CommonUtils.Companion.BitmapPool.getBitmap(bitmapWidth, maxOf(1, bitmapHeight))
                    renderer.renderPage(position, bitmap) { success, pageNo, renderedBitmap ->
                        if (success && pageNo == position) {
                            CoroutineScope(Dispatchers.Main).launch {
                                pageView.setImageBitmap(renderedBitmap ?: bitmap)
                                applyFadeInAnimation(pageView)
                                pageLoadingLayout.pdfViewPageLoadingProgress.visibility = View.GONE
                            }
                        } else {
                            CommonUtils.Companion.BitmapPool.recycleBitmap(bitmap)
                        }
                    }
                }
            }
        }

        private fun ListItemPdfPageBinding.updateLayoutParams(height: Int) {
            root.layoutParams = root.layoutParams.apply {
                this.height = height
                (this as? ViewGroup.MarginLayoutParams)?.setMargins(
                    pageSpacing.left, pageSpacing.top, pageSpacing.right, pageSpacing.bottom
                )
            }
        }

        private fun applyFadeInAnimation(view: View) {
            view.startAnimation(AlphaAnimation(0F, 1F).apply {
                interpolator = LinearInterpolator()
                duration = 300
            })
        }

        private fun handleLoadingForPage(position: Int) {
            itemBinding.pageLoadingLayout.pdfViewPageLoadingProgress.visibility =
                if (enableLoadingForPages && !renderer.pageExistInCache(position)) View.VISIBLE else View.GONE
        }
    }
}

