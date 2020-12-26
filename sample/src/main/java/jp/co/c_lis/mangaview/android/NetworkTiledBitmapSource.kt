package jp.co.c_lis.mangaview.android

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import dev.keiji.mangaview.TiledSource
import dev.keiji.mangaview.source.TiledBitmapSource
import dev.keiji.mangaview.widget.ViewContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

class NetworkTiledBitmapSource(
    tiledSource: TiledSource,
    private val urlList: List<String>,
    private val tmpDir: File,
    private val coroutineScope: CoroutineScope
) : TiledBitmapSource(tiledSource) {

    companion object {
        private val TAG = NetworkTiledBitmapSource::class.java.simpleName

        private const val RETRY_INTERVAL = 10 * 1000L
        private const val MAX_RETRY_COUNT = 5
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
            val fileName = Uri.parse(urlList[tile.index]).lastPathSegment ?: return@launch
            val tmpFilePath = File(tmpDir, fileName)

            if (!tmpFilePath.exists()) {
                var retryCount = 0

                while (
                    !downloadTiledBitmap(URL(urlList[tile.index]), tmpFilePath)
                    && retryCount < MAX_RETRY_COUNT
                ) {
                    delay(RETRY_INTERVAL)
                    retryCount++
                }
            }

            if (tmpFilePath.exists()) {
                val bitmap = FileInputStream(tmpFilePath).use {
                    BitmapFactory.decodeStream(it, null, options)
                }

                if (bitmap != null) {
                    cacheBin[tile] = bitmap
                } else {
                    Log.e(TAG, "Bitmap decoding error occurred.")
                    if (tmpFilePath.delete()) {
                        Log.e(TAG, "Cache file ${tmpFilePath.absolutePath} has been deleted.")
                    }
                }
            } else {
                Log.e(TAG, "File ${tmpFilePath.name} not found. May download process failed.")
            }

            jobMap.remove(tile)
        }

        return null
    }

    private fun downloadTiledBitmap(url: URL, tmpFilePath: File): Boolean {
        return try {
            FileOutputStream(tmpFilePath).use { outputStream ->
                val conn = url.openConnection().also {
                    it.connect()
                }
                conn.getInputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
                outputStream.flush()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, e.javaClass.simpleName, e)
            tmpFilePath.delete()
            false
        }
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
