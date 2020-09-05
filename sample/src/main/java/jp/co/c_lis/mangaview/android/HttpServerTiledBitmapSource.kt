package jp.co.c_lis.mangaview.android

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import dev.keiji.mangaview.TiledSource
import dev.keiji.mangaview.widget.TiledBitmapSource
import dev.keiji.mangaview.widget.ViewContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

class HttpServerTiledBitmapSource(
    tiledSource: TiledSource,
    private val urlList: List<String>,
    private val tmpDir: File,
    private val coroutineScope: CoroutineScope
) : TiledBitmapSource(tiledSource) {

    companion object {
        private val TAG = HttpServerTiledBitmapSource::class.java.simpleName
    }

    @Volatile
    private var jobMap = ConcurrentHashMap<TiledSource.Tile, Job>()

    private val options = BitmapFactory.Options().also {
        it.inPreferredConfig = Bitmap.Config.RGB_565
    }

    override fun load(tile: TiledSource.Tile): Bitmap? {
        val tileBitmap = cacheBin[tile]
        if (tileBitmap != null && !tileBitmap.isRecycled) {
            return tileBitmap
        }

        if (jobMap.containsKey(tile)) {
            return null
        }

        jobMap[tile] = coroutineScope.launch(Dispatchers.IO) {
            val url = URL(urlList[tile.index])
            val fileName = Uri.parse(urlList[tile.index]).lastPathSegment ?: return@launch
            val tmpFilePath = File(tmpDir, fileName)

            if (!tmpFilePath.exists()) {
                FileOutputStream(tmpFilePath).use { outputStream ->
                    val conn = url.openConnection().also {
                        it.connect()
                    }
                    conn.getInputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    outputStream.flush()
                }
            }

            val bitmap = FileInputStream(tmpFilePath).use {
                BitmapFactory.decodeStream(it, null, options)
            }

            if (bitmap == null) {
                Log.e(TAG, "Bitmap decoding error occurred.")
                tmpFilePath.deleteOnExit()
            } else {
                cacheBin[tile] = bitmap
            }

            jobMap.remove(tile)
        }

        return null
    }

    override fun prepare(viewContext: ViewContext, onImageSourceLoaded: () -> Unit): Boolean {
        onImageSourceLoaded()
        return true
    }

    override fun getState(viewContext: ViewContext): State {
        return State.Prepared
    }

    override fun recycle(tile: TiledSource.Tile) {
        jobMap[tile]?.cancel()
        jobMap.remove(tile)

        super.recycle(tile)
    }

    override fun recycle() {
        jobMap.values.forEach { job ->
            job.cancel()
        }
        jobMap.clear()

        super.recycle()
    }
}
