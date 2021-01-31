package jp.co.c_lis.mangaview.android

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import dev.keiji.mangaview.source.BitmapSource
import dev.keiji.mangaview.widget.ViewContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AssetBitmapSource(
    private val assetManager: AssetManager,
    private val fileName: String,
    private val coroutineScope: CoroutineScope
) : BitmapSource() {

    override val bitmap: Bitmap?
        get() = assetBitmap

    private var assetBitmap: Bitmap? = null

    override fun recycle() {
        job?.cancel()
        job = null

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

    private val options = BitmapFactory.Options()

    private var job: Job? = null

    override fun prepare(viewContext: ViewContext, onPrepared: () -> Unit): Boolean {
        if (getState(viewContext) != State.NA) {
            return true
        }

        job = coroutineScope.launch(Dispatchers.IO) {
            assetBitmap = assetManager.open(fileName).use {
                BitmapFactory.decodeStream(it, null, options)
            }
            onPrepared()
            job = null
        }

        return false
    }


}
