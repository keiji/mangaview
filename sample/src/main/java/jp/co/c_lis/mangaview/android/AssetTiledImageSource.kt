package jp.co.c_lis.mangaview.android

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import dev.keiji.mangaview.widget.TiledImageSource
import dev.keiji.mangaview.widget.TiledSource
import dev.keiji.mangaview.widget.ViewContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AssetTiledImageSource(
    tiledSource: TiledSource,
    private val tiledFileDir: String,
    private val assetManager: AssetManager,
    private val coroutineScope: CoroutineScope
) : TiledImageSource(tiledSource) {

    companion object {
        private val TAG = AssetTiledImageSource::class.java.simpleName
    }

    @Volatile
    private var jobMap = HashMap<TiledSource.Tile, Job>()

    private val options = BitmapFactory.Options().also {
        it.inPreferredConfig = Bitmap.Config.RGB_565
    }

    override fun load(tile: TiledSource.Tile): Bitmap? {
        val tileBitmap = cacheBin[tile]
        if (tileBitmap != null) {
            return tileBitmap
        }

        if (jobMap.containsKey(tile)) {
            return null
        }

        jobMap[tile] = coroutineScope.launch(Dispatchers.IO) {
            val fileList = assetManager.list(tiledFileDir) ?: return@launch
            val filePath = tiledFileDir + '/' + fileList[tile.index]
            val bitmap = assetManager.open(filePath).use {
                BitmapFactory.decodeStream(it, null, options)
            }
            cacheBin[tile] = bitmap
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

    override fun recycle() {
        super.recycle()

        jobMap.values.forEach { job ->
            job.cancel()
        }
        jobMap.clear()
    }
}
