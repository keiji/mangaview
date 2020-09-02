package jp.co.c_lis.mangaview.android

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorSpace
import android.util.Log
import dev.keiji.mangaview.widget.TiledBitmapLayer
import dev.keiji.mangaview.widget.TiledSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AssetTiledBitmapLayer(
    tiledSource: TiledSource,
    private val assetManager: AssetManager,
    private val tiledFileDir: String,
    private val coroutineScope: CoroutineScope,
) : TiledBitmapLayer(tiledSource) {

    @Volatile
    private var jobMap = HashMap<TiledSource.Tile, Job>()

    companion object {
        private val TAG = AssetTiledBitmapLayer::class.java.simpleName
    }

    private val options = BitmapFactory.Options().also {
        it.inPreferredConfig = Bitmap.Config.RGB_565
    }

    override fun onContentPrepared(tile: TiledSource.Tile): Boolean {
        if (jobMap.containsKey(tile)) {
            return false
        }

        jobMap[tile] = coroutineScope.launch(Dispatchers.IO) {
            val fileList = assetManager.list(tiledFileDir) ?: return@launch
            val filePath = tiledFileDir + '/' + fileList[tile.index]
            val bitmap = assetManager.open(filePath).use {
                BitmapFactory.decodeStream(it, null, options)
            }
            synchronized(cacheBin) {
                cacheBin[tile] = bitmap
            }
            jobMap.remove(tile)
        }

        return false
    }

    override fun onRecycled() {
        synchronized(jobMap) {
            jobMap.values.forEach { job ->
                job.cancel()
            }
            jobMap.clear()
        }

        super.onRecycled()
    }
}
