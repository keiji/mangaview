package jp.co.c_lis.mangaview.android

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import dev.keiji.mangaview.widget.BitmapImageSource
import dev.keiji.mangaview.widget.ViewContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AssetBitmapImageSource(
    private val assetManager: AssetManager,
    private val fileName: String,
    private val coroutineScope: CoroutineScope
) : BitmapImageSource() {

    override val bitmap: Bitmap?
        get() = assetBitmap

    private var assetBitmap: Bitmap? = null

    override fun recycle() {
        assetBitmap?.recycle()
        assetBitmap = null
    }

    override fun getState(viewContext: ViewContext): State {
        return when {
            bitmap != null -> State.Prepared
            job != null -> State.Preparing
            else -> State.NA
        }
    }

    private val options = BitmapFactory.Options().also {
        it.inPreferredConfig = Bitmap.Config.RGB_565
    }

    private var job: Job? = null

    override fun load(viewContext: ViewContext, onImageSourceLoaded: () -> Unit): Boolean {
        if (getState(viewContext) != State.NA) {
            return true
        }

        job = coroutineScope.launch(Dispatchers.IO) {
            assetBitmap = assetManager.open(fileName).use {
                BitmapFactory.decodeStream(it, null, options)
            }
            onImageSourceLoaded()
            job = null
        }

        return false
    }


}
