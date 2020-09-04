package jp.co.c_lis.mangaview.android

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.graphics.Path
import dev.keiji.mangaview.Region
import dev.keiji.mangaview.widget.PathSource
import dev.keiji.mangaview.widget.ViewContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AssetPathSource(
    private val assetManager: AssetManager,
    private val fileName: String,
    private val regionList: List<Region>,
    private val coroutineScope: CoroutineScope
) : PathSource() {

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

                regionList.forEach { region ->
                    pathList.add(region.toPath(bitmapWidth, bitmapHeight))
                }

                job = null
            }
            return false
        }

        onImageSourceLoaded()

        return true
    }

    override fun recycle() {

    }
}
