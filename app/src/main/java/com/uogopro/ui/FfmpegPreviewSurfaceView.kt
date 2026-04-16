package com.uogopro.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import org.bytedeco.javacv.AndroidFrameConverter
import org.bytedeco.javacv.FFmpegFrameGrabber

class FfmpegPreviewSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : SurfaceView(context, attrs), SurfaceHolder.Callback {
    @Volatile
    var streamUri: String = ""
        private set

    @Volatile
    private var running = false

    @Volatile
    private var surfaceReady = false

    @Volatile
    private var grabber: FFmpegFrameGrabber? = null

    private var worker: Thread? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    init {
        holder.addCallback(this)
        setZOrderOnTop(false)
    }

    fun start(uri: String) {
        if (streamUri == uri && running) return
        requestStop()
        streamUri = uri
        if (surfaceReady) {
            startWorker(uri)
        }
    }

    fun requestStop() {
        running = false
        worker?.interrupt()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        surfaceReady = true
        if (streamUri.isNotBlank()) {
            startWorker(streamUri)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) = Unit

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        surfaceReady = false
        requestStop()
    }

    override fun onDetachedFromWindow() {
        requestStop()
        super.onDetachedFromWindow()
    }

    private fun startWorker(uri: String) {
        if (running || worker?.isAlive == true) return
        running = true
        worker = Thread({ decodeLoop(uri) }, "gopro-ffmpeg-preview").apply {
            isDaemon = true
            start()
        }
    }

    private fun decodeLoop(uri: String) {
        val converter = AndroidFrameConverter()
        var localGrabber: FFmpegFrameGrabber? = null
        try {
            localGrabber = FFmpegFrameGrabber(uri).apply {
                format = "mpegts"
                setOption("fflags", "nobuffer")
                setOption("flags", "low_delay")
                setOption("probesize", "8192")
                setOption("analyzeduration", "0")
                setOption("max_delay", "0")
                setOption("reorder_queue_size", "0")
                start()
            }
            grabber = localGrabber

            while (running && surfaceReady && !Thread.currentThread().isInterrupted) {
                val frame = localGrabber.grabImage() ?: continue
                val bitmap = converter.convert(frame) ?: continue
                drawBitmap(bitmap)
            }
        } catch (_: Throwable) {
            drawErrorFrame()
        } finally {
            running = false
            runCatching { localGrabber?.stop() }
            runCatching { localGrabber?.release() }
            if (grabber === localGrabber) {
                grabber = null
            }
            if (worker === Thread.currentThread()) {
                worker = null
            }
        }
    }

    private fun drawBitmap(bitmap: Bitmap) {
        if (!surfaceReady || !running) return
        val canvas = holder.lockCanvas() ?: return
        try {
            canvas.drawColor(Color.BLACK)
            val dst = fitCenterRect(
                sourceWidth = bitmap.width.toFloat(),
                sourceHeight = bitmap.height.toFloat(),
                targetWidth = canvas.width.toFloat(),
                targetHeight = canvas.height.toFloat(),
            )
            canvas.drawBitmap(bitmap, null, dst, paint)
        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun drawErrorFrame() {
        if (!surfaceReady) return
        val canvas = holder.lockCanvas() ?: return
        try {
            canvas.drawColor(Color.BLACK)
        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun fitCenterRect(
        sourceWidth: Float,
        sourceHeight: Float,
        targetWidth: Float,
        targetHeight: Float,
    ): RectF {
        if (sourceWidth <= 0f || sourceHeight <= 0f || targetWidth <= 0f || targetHeight <= 0f) {
            return RectF(0f, 0f, targetWidth, targetHeight)
        }
        val scale = minOf(targetWidth / sourceWidth, targetHeight / sourceHeight)
        val width = sourceWidth * scale
        val height = sourceHeight * scale
        val left = (targetWidth - width) / 2f
        val top = (targetHeight - height) / 2f
        return RectF(left, top, left + width, top + height)
    }
}
