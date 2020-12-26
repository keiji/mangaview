package jp.co.c_lis.mangaview.android

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.util.Log
import dev.keiji.mangaview.Region
import dev.keiji.mangaview.source.RegionSource
import dev.keiji.mangaview.widget.ViewContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception
import java.net.URI
import java.net.URL
import java.util.Collections

/**
 * MRDB(Manga Region Database).
 */
class MrdbFrameRegionSource(
    private val assetManager: AssetManager,
    private val catalogFileName: String,
    private val fileName: String,
    private val tmpDir: File,
    private val coroutineScope: CoroutineScope
) : RegionSource() {
    companion object {
        val TAG = MrdbFrameRegionSource::class.java.simpleName

        private const val CATEGORY_COMIC = 1
        private const val LABEL_FRAME = 0

        private const val RETRY_INTERVAL = 10 * 1000L
        private const val MAX_RETRY_COUNT = 5
    }

    override val regionList: MutableList<Region> = Collections.synchronizedList(ArrayList<Region>())

    override val contentWidth: Float
        get() = bitmapWidth

    override val contentHeight: Float
        get() = bitmapHeight

    @Volatile
    private var bitmapWidth: Float = -1.0F

    @Volatile
    private var bitmapHeight: Float = -1.0F

    override fun getState(viewContext: ViewContext): State {
        return State.Prepared
    }

    private val options = BitmapFactory.Options().also {
        it.inJustDecodeBounds = true
    }

    private var job: Job? = null

    private suspend fun getRegionList() = withContext(Dispatchers.IO) {

        // All MrdbFrameRegionSource will read whole file every loading.
        // This implementation is completely for test use.
        val jsonStr = assetManager.open(catalogFileName)
            .bufferedReader()
            .readText()

        val jsonObj = JSONObject(jsonStr)
        if (!jsonObj.has(fileName)) {
            return@withContext
        }

        val fileObj = jsonObj.getJSONObject(fileName)
        val urlStr = fileObj.getString("url")
        val uri = URI.create(urlStr)

        val imageId = uri.path
            .split("/")
            .last { item -> item.isNotEmpty() }
        val tmpFilePath = File(tmpDir, imageId)

        if (!tmpFilePath.exists() || tmpFilePath.length() == 0L) {
            var retryCount = 0

            @Suppress("BlockingMethodInNonBlockingContext")
            while (!getRegionList(URL(urlStr), tmpFilePath)
                && retryCount < MAX_RETRY_COUNT
            ) {
                delay(RETRY_INTERVAL)
                retryCount++
            }

        }

        if (!tmpFilePath.exists()) {
            Log.e(TAG, "File ${tmpFilePath.name} not found. May download process failed.")
            return@withContext
        }

        @Suppress("BlockingMethodInNonBlockingContext")
        val regionsStr = FileInputStream(tmpFilePath)
            .bufferedReader()
            .readText()
        val regionArray = JSONObject(regionsStr)
            .getJSONArray("regions")

        for (index in (0 until regionArray.length())) {
            val regionObj = regionArray.getJSONObject(index)
            val categoryId = regionObj.getInt("category_id")
            val label = regionObj.getInt("label")
            val pointJsonArray = regionObj.getJSONArray("points")

            val pointArray = ArrayList<PointF>()
            for (j in (0 until pointJsonArray.length())) {
                val pointObj = pointJsonArray.getJSONObject(j)
                val point = PointF(
                    pointObj.getDouble("x").toFloat(),
                    pointObj.getDouble("y").toFloat()
                )
                pointArray.add(point)
            }

            if (categoryId == CATEGORY_COMIC && label == LABEL_FRAME) {
                regionList.add(Region(categoryId, label, pointList = pointArray))
            }
        }
    }

    private fun getRegionList(url: URL, tmpFilePath: File): Boolean {
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
            Log.d(TAG, e.javaClass.simpleName, e)
            tmpFilePath.delete()
            false
        }
    }

    override fun prepare(viewContext: ViewContext, onImageSourceLoaded: () -> Unit): Boolean {

        if (bitmapWidth < 0 || bitmapHeight < 0) {
            if (job != null) {
                return false
            }

            job = coroutineScope.launch(Dispatchers.IO) {
                assetManager.open(fileName).use {
                    BitmapFactory.decodeStream(it, null, options)
                    bitmapWidth = options.outWidth.toFloat()
                    bitmapHeight = options.outHeight.toFloat()
                }

                getRegionList()

                job = null
            }

            return false
        }

        onImageSourceLoaded()

        return true
    }

    override fun recycle() {
        job?.cancel()
        job = null

        bitmapWidth = -1.0F
        bitmapHeight = -1.0F
        pathList.clear()
    }
}
