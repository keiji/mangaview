package jp.co.c_lis.mangaview.android

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Path
import dev.keiji.mangaview.widget.PathSource
import dev.keiji.mangaview.widget.ViewContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AssetPathSource(
    private val assetManager: AssetManager,
    private val fileName: String,
    private val width: Float,
    private val height: Float,
    private val coroutineScope: CoroutineScope
) : PathSource() {

    override val contentWidth: Float
        get() =  width

    override val contentHeight: Float
        get() =  height

    override fun getState(viewContext: ViewContext): State {
        return State.Prepared
    }

    override fun prepare(viewContext: ViewContext, onImageSourceLoaded: () -> Unit): Boolean {
        val path = Path().also {
            it.lineTo(0.0F, 150F)
            it.lineTo(150F, 150F)
            it.lineTo(150F, 0.0F)
            it.lineTo(0.0F, 0.0F)
            it.close()
        }
        pathList.add(path)

        onImageSourceLoaded()

        return true
    }

    override fun recycle() {

    }
}
